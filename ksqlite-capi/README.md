# Module Ksqlite C API

Almost a one-to-one mapping of the SQLite C API, with a few type adjustments taking advantage of
what Kotlin offers. See [Types](../ksqlite-types/core/README.md) for the different parameter and
result types you'll run into.

This module stays close to the C API's own shape on purpose: opaque handles, output parameters,
raw SQLite semantics.

## Table of contents

- [Conventions](#conventions)
- [Initialization](#initialization)
- [Memory management](#memory-management)
- [Native handles](#native-handles)
- [Output parameters](#output-parameters)
- [Buffers](#buffers)
- [Variadic configuration functions](#variadic-configuration-functions)
- [Callbacks](#callbacks)
- [Virtual tables](#virtual-tables)
- [Virtual file systems](#virtual-file-systems)
- [Encryption](#encryption)

## Conventions

### Documentation

SQLite's own documentation is the only complete reference. Most KDoc comments in this module are
carried over from SQLite's own sources, `sqlite3.c` included, rather than written from scratch.
Where this module's behavior diverges from SQLite's, or needs a Kotlin-specific note, its own
KDoc adds that on top.

### Thread safety

This module adds no thread-safety guarantees beyond what SQLite itself provides. It does assume
concurrent usage on targets that support it, and keeps its own memory management, the registries
described below, safe under that assumption.

### Booleans

Some SQLite routines return or accept an integer meant to be interpreted as `true` or `false`.
Unlike the rest of this module, interpreting that meaning is left to the caller here. These
integers stay `kotlin.Int`, not `kotlin.Boolean`. `ksqlite-kapi` is the layer that turns them into
real booleans.

### Default values

None of the `sqlite3_*` function bindings in this module supply a default value for a parameter.
If one is found, it is a mistake and should be reported. This is deliberate. Every call forces the
caller to read the [SQLite documentation](https://sqlite.org/docs.html) and understand what SQLite does with the
exact arguments supplied, and what side effects to expect.

## Initialization

Loading the Ksqlite library and initializing SQLite are two different things. The former is
automatic. This module's native library is loaded the first time one of its functions is called.
The latter isn't, and every other function in this module assumes it already happened.

`SQLITE_OMIT_AUTOINIT` is enabled on every build, as
[SQLite itself recommends](https://sqlite.org/compile.html#recommended_compile_time_options), so
nothing calls `sqlite3_initialize()` on this module's behalf. `sqlite3_config()` is the only
SQLite API that may be called before it:

```kotlin
val initResult = sqlite3_initialize()

if (!initResult.isOk) {
    error("SQLite initialization failed: $initResult")
}

// open connections, run queries, ...

check(sqlite3_shutdown().isOk)
```

> [!WARNING]
> Skipping `sqlite3_initialize()` is a segfault your VM won't survive. `sqlite3_shutdown()` must
> be called once you have finished using SQLite, especially if encryption is enabled, as
> [SQLite3MultipleCiphers' author recommends](https://utelle.github.io/SQLite3MultipleCiphers/).

## Memory management

There is close to nothing to manage by hand. This module pins a resource when needed, or
allocates native memory to hold one, and releases it at the earliest opportunity. Resources tied
to an SQLite entity with a clearly defined lifecycle, an `sqlite3` connection or an `sqlite3_stmt`
statement among them, are released in either of two ways:

- SQLite invokes their `xDestroy` callback.
- The entity itself is released, through `sqlite3_close()`, `sqlite3_finalize()`, and so on.

A few resources, listed below, need to be released by hand instead, to avoid a memory leak or a
callback firing when the application no longer expects it.

### Global callbacks

Some callbacks are registered globally, process-wide, rather than tied to a connection or a
statement. `sqlite3_config()`'s `LOG` and `SQLLOG` options are the two examples in this module.
Since there is no entity whose closing would clean them up, resetting one is done the same way it
was set, by calling `sqlite3_config()` again with the callback set to `null`:

```kotlin
// Sets the callback, allocating a native resource
sqlite3_config(SqliteConfigOption.LOG(appData) { appData, errorCode, errorMessage ->
    // ...
})

// Clears the callback, releasing the native resource
sqlite3_config(SqliteConfigOption.LOG(null, null))
```

### Structs

Some structs are owned by whoever instantiates them, and releasing them is that owner's
responsibility. The structs below are all identifiable by extending `ClosableStruct`:

- `sqlite3_file`
- `sqlite3_module`
- `sqlite3_vtab`
- `sqlite3_vtab_cursor`

Once no longer required, they should be closed to prevent a memory leak.

```kotlin
val vfs = sqlite3_vfs_find(null) ?: error("No default VFS")
val file = sqlite3_file(vfs)
// ...
file.close()
```

Some of them can be subclassed by the application.

> [!WARNING]
> A subclass that overrides `close` must call `super.close()`.

### Native buffers

A `Buffer` obtained from `sqlite3_malloc()` or `sqlite3_malloc64()` needs `sqlite3_free()`. This
module never calls `sqlite3_free()` on a `Buffer` it did not allocate itself, the owner is
responsible for freeing it wherever that is appropriate.

```kotlin
val buffer = sqlite3_malloc64(size) ?: error("Allocation failed")
// ... use buffer ...
sqlite3_free(buffer)
```

## Native handles

Every opaque SQLite C pointer, `sqlite3`, `sqlite3_stmt`, `sqlite3_value`, `sqlite3_blob`,
`sqlite3_backup`, `sqlite3_context`, and `sqlite3_snapshot`, is a `Struct`.

Two `Struct` instances pointing at the same address are guaranteed to compare equal (`==`). A
method such as `sqlite3_db_handle()` may therefore return a different `sqlite3` instance than the
one at the origin of the `sqlite3_stmt` argument, while both still represent the same native
`sqlite3` object.

## Output parameters

SQLite's C API leans on output parameters, a pointer the callee writes its result into, for
anything a single return value cannot carry. Kotlin has no equivalent to that, so each one
becomes a small `OutputParam<T>` instance instead, read once the call returns. Primitives get
`Int32OutputParam` and `Int64OutputParam`, strings get `Utf8OutputParam`, and every `Struct` type
carries its own nested `OutputParam` for handles:

```kotlin
val outDb = sqlite3.OutputParam()

val openResult = sqlite3_open_v2(
    fileName = "myDb.db",
    outDb = outDb,
    flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
    vfs = null
)

val db = outDb.value // Note that db may be null in case of failure
```

## Buffers

Blob content and the memory pools some configuration options accept move through `Buffer` and
`ReadableBuffer` instead of a plain `ByteArray`. Their content can live in native memory rather
than on the Kotlin heap. `sqlite3_malloc()` and `sqlite3_malloc64()` return an owned, writable
`Buffer`:

```kotlin
val textBuffer = checkNotNull(sqlite3_malloc(text.size))
textBuffer.write(text)

sqlite3_bind_text64(stmt, 3, textBuffer, textBuffer.byteSize, SqliteTextEncoding.UTF8) { buffer ->
    // called once SQLite is done with textBuffer
    sqlite3_free(buffer)
}
```

`sqlite3_column_buffer()` and `sqlite3_value_buffer()` return a read-only `ReadableBuffer`
instead, a view directly over memory SQLite itself owns. It's valid only for as long as the
current row or value is:

```kotlin
val blobBuffer = checkNotNull(sqlite3_column_buffer(stmt, 4))
val chunk = blobBuffer.read(chunkSize, offset)
```

`sqlite3_deserialize()` can go either way. Passed `flags = null` as below, the allocating side
keeps ownership and must free the buffer once the database is done with it. Passing
`SqliteDeserializeFlag.FREEONCLOSE` instead hands ownership to SQLite, which frees the buffer
itself once the connection closes, even if the call fails, so the caller must not call
`sqlite3_free()` on it in that case:

```kotlin
val buffer = sqlite3_malloc64(bufferSize) ?: error("Allocation failed")
buffer.write(content)

val result = sqlite3_deserialize(db, "main", buffer, dbSize, bufferSize, flags = null)

if (!result.isOk) {
    error("Deserialization failed")
}

// ... use db ...

sqlite3_free(buffer)
```

## Variadic configuration functions

`sqlite3_config()`, `sqlite3_db_config()`, and `sqlite3_vtab_config()` are all C varargs
functions. What arguments follow the first one, and their type, depends on it. Each option is
instead a case of its own sealed class, carrying exactly the arguments it needs:

```kotlin
val lookaside = SqliteConfigOption.LOOKASIDE(sz = 128, cnt = 2)
val result = sqlite3_config(lookaside)
```

```kotlin
val dbName = SqliteDbConfigOption.MAINDBNAME("primary")
val result = sqlite3_db_config(db, dbName)
```

```kotlin
val constraintSupport = SqliteVtabConfigOption.CONSTRAINT_SUPPORT(1)
val result = sqlite3_vtab_config(db, constraintSupport)
```

Internally, each option is turned into the argument array its specific C call actually expects
and dispatched through whatever variadic mechanism the target provides. None of that shows up at
the call site above.

`sqlite3_file_control()` is not itself variadic, but works the same way:

```kotlin
val outName = Utf8OutputParam()
val vfsName = SqliteFileControlOpcode.VFSNAME(outName)
val result = sqlite3_file_control(db, null, vfsName)

if (result.isOk) {
    val name = checkNotNull(outName.value)
}
```

## Callbacks

SQLite calls back into the application through plain function pointers for hooks (the authorizer,
the update hook, trace, the busy handler, and others) and for SQL functions. Every one of these is
a plain Kotlin functional interface on this module's side. Except for a few cases, its
registration function also takes an `appData` value handed back on every invocation.

`appData` exists because the C API has no closures of its own, but a Kotlin lambda can already
capture whatever it needs, so using it is entirely optional. Capturing state directly reads the
most natural in a one-off registration:

```kotlin
val allowedTables = setOf("fruits", "vegetables")

sqlite3_set_authorizer(db, appData = null) { _, action, table, _, _, _ ->
    if (action == SqliteActionCode.CREATE_TABLE && table !in allowedTables) {
        SqliteAuthorizerStatus.DENY
    } else {
        SqliteAuthorizerStatus.OK
    }
}
```

`appData` is more convenient once the same callback logic is reused across connections, or
implemented as a standalone type or top-level value instead of an inline lambda:

```kotlin
val tableWhitelistAuthorizer =
    SqliteAuthorizerCallback<Set<String>> { appData, action, detail1, _, _, _ ->
        if (action == SqliteActionCode.CREATE_TABLE && detail1 !in appData) {
            SqliteAuthorizerStatus.DENY
        } else {
            SqliteAuthorizerStatus.OK
        }
    }

fun setupDb(db: sqlite3) {
    sqlite3_set_authorizer(db, setOf("fruits", "vegetables"), tableWhitelistAuthorizer)
}
```

Both styles behave identically. Pick whichever reads better for the callback at hand.

A few callbacks need to hand back a value through an output pointer in addition to a result code,
more than a single Kotlin return value can carry. Those are declared as an extension function on a
`Scope` receiver instead of a plain function. The receiver supplies whatever factory methods fit
that callback's possible outcomes, each producing the `Result` the callback returns, so one
expression carries everything at once. `success(value)`/`failure(...)` is the most common shape,
used by `sqlite3_auto_extension()`'s callback and most virtual table callbacks, but not all of
them follow it:

```kotlin
val findFunction = SqliteVtabFindFunctionCallback<MyTable> { _, _, functionName ->
    if (functionName == "my_custom_func") {
        overload { _, context, values -> /* ... */ }
    } else {
        doNotOverload()
    }
}
```

`xFindFunction`'s `Scope` has no `success`/`failure` at all, only `overload(...)` and
`doNotOverload()`, since it isn't reporting an outcome but whether it recognized the function
SQLite is asking about.

## Virtual tables

A virtual table is registered by building an `sqlite3_module` out of named callback parameters
and handing it to `sqlite3_create_module()` or `sqlite3_create_module_v2()`. The table itself,
and its cursors, are plain Kotlin classes extending `sqlite3_vtab` and `sqlite3_vtab_cursor`, free
to carry whatever state the implementation needs as regular properties.

`connect`/`create`, `open`, and `rowid` below use the `Scope` receiver from
[Callbacks](#callbacks), since each needs to hand back a value alongside the result code.

Both the table and its cursor are allocated by the application, and SQLite never frees them on its
own. Closing the ones handed out here is the implementation's job, inside `disconnect`/`destroy`
for the table and `close` for the cursor. This mirrors the C API itself, where xDisconnect,
xDestroy, and xClose are responsible for freeing whatever struct they were handed:

```kotlin
private class MyTable : sqlite3_vtab() {
    val rows = mutableListOf<Row>()
}

private class MyCursor(val table: MyTable) : sqlite3_vtab_cursor() {
    var position = 0
}

val module = sqlite3_module(
    version = SqliteModuleVersion.VERSION_4,
    create = null,
    connect = { db, appData: Nothing?, argv ->
        sqlite3_declare_vtab(db, "CREATE TABLE x(id INTEGER PRIMARY KEY, name TEXT)")
        success(MyTable())
    },
    bestIndex = { _, info -> 
        /* pick an index strategy for info */ 
        SqliteResultCode.OK 
    },
    disconnect = { table ->
        table.close()
        SqliteResultCode.OK
    },
    destroy = { table ->
        table.close()
        SqliteResultCode.OK
    },
    open = { table -> 
        success(MyCursor(table)) 
    },
    close = { cursor ->
        cursor.close()
        SqliteResultCode.OK
    },
    filter = { cursor, _, _, _ ->
        cursor.position = 0
        SqliteResultCode.OK
    },
    next = { cursor ->
        cursor.position++
        SqliteResultCode.OK
    },
    eof = { cursor -> 
        if (cursor.position >= cursor.table.rows.size) 1 else 0 
    },
    column = { cursor, context, columnIndex ->
        /* write the row's value into context */
        SqliteResultCode.OK
    },
    rowid = { cursor ->
        success(cursor.table.rows[cursor.position].id) 
    },
    update = null,
    findFunction = null,
    begin = null,
    sync = null,
    commit = null,
    rollback = null,
    rename = null,
    savepoint = null,
    release = null,
    rollbackTo = null,
    integrity = null
)

val result = sqlite3_create_module_v2(db, "my_table", module, appData = null) {
    module.close() // If appropriated
}
```

The eleven callbacks kept above are the minimum needed for any table, even a read-only one.
Everything left `null` here is genuinely optional: `update` for writes, `begin`/`sync`/`commit`/
`rollback`/`savepoint`/`release`/`rollbackTo` for transactions, `rename`, `findFunction`, and
`integrity`. Add one only once the table actually needs to support it.

Whether a table is eponymous depends entirely on `create` and `connect`. Leaving `create` `null`,
as the example above does, makes the table eponymous-only: usable anonymously, as in
`SELECT * FROM my_table(...)`, but never through `CREATE VIRTUAL TABLE`. Passing the exact same
callback reference (`===`) for both makes it eponymous instead, usable both ways. Giving each a
distinct callback is the regular case, one virtual table type per module, created only through
`CREATE VIRTUAL TABLE`.

## Virtual file systems

Traces of VFS functions and structs are present and public, but they were initially written for
evaluation purposes. They turned out useful in this module's own test suite, so they were kept.
VFS support itself is not official yet.

## Encryption

[SQLite3MultipleCiphers](https://github.com/utelle/SQLite3MultipleCiphers) is bundled with every
build, adding transparent database encryption on top of the plain SQLite C API.

`sqlite3_key()`/`sqlite3_key_v2()` set a connection's passphrase, right after `sqlite3_open()`.
`sqlite3_rekey()`/`sqlite3_rekey_v2()` change it, encrypting a previously plaintext database on the
first call, or decrypting one if `key`/`nKey` is empty. The `_v2` variants target a specific schema
(`zDbName`, `main`/`temp`/an attached database's name) instead of always the main one:

```kotlin
val key = "correct horse battery staple".encodeToByteArray()
val keyResult = sqlite3_key(db, key, key.size)
```

`sqlite3mc_config()`/`sqlite3mc_config_cipher()` read and write encryption options, process-wide
(`SqliteMcConfig`, `db = null` for the compile-time default) or per-cipher
(`SqliteMcConfigCipherParam`, `SqliteMcCodecType.CHACHA20.KDF_ITER` and similar). Both take a
`prefix` (`SqliteMcConfigParamPrefix`) this module adds on top of the raw C API, controlling
whether the transient or the permanent value is read or written:

```kotlin
val cipherResult = sqlite3mc_config(db, SqliteMcConfig.CIPHER, Default, SqliteMcCodecType.CHACHA20)
val kdfIter = sqlite3mc_config_cipher(db, SqliteMcCodecType.CHACHA20, SqliteMcCodecType.CHACHA20.KDF_ITER, None)
```

`sqlite3mc_cipher_count()`/`sqlite3mc_cipher_index()`/`sqlite3mc_cipher_name()` walk the registry of
ciphers currently known to SQLite3MultipleCiphers, builtin and dynamically registered alike.
`sqlite3mc_codec_data()` reads back per-connection encryption state, the salt among it.

`sqlite3mc_register_cipher()` plugs a cipher implemented in Kotlin into SQLite3MultipleCiphers.
`descriptor`, a `CipherDescriptor` (a `ClosableStruct`, see [Structs](#structs)), is built from ten
callbacks covering the whole cipher lifecycle, `allocate`, `free`, `clone`, `getLegacy`,
`getPageSize`, `getReserved`, `getSalt`, `generateKey`, `encryptPage`, `decryptPage`. `params`, a
`StructArray<CipherParams>`, declares whatever custom configuration parameters the cipher exposes,
and must end with a sentinel entry, an empty `m_name`, even for a cipher that takes none:

```kotlin
val params = checkNotNull(CipherParams.allocateArray(1) { m_name = "" })
val registerResult = sqlite3mc_register_cipher(descriptor, params, makeDefault = 0)
```

`sqlite3mc_vfs_create()`/`sqlite3mc_vfs_destroy()`/`sqlite3mc_vfs_shutdown()` wrap an existing VFS
with one that applies SQLite3MultipleCiphers' encryption transparently, see
[Virtual file systems](#virtual-file-systems).

> [!WARNING]
> `sqlite3_shutdown()` must be called before the process exits once encryption was used, see
> [Initialization](#initialization).
