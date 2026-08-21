# Module Ksqlite Kotlin API

`ksqlite-kapi` is an object-oriented, Kotlin-shaped API for SQLite, built on top of
[`ksqlite-capi`](../ksqlite-capi/README.md) without exposing it, don't depend on both at once. See
the [root README](../README.md#choosing-an-implementation) for how the two compare.

`ksqlite-capi` mirrors the SQLite C API closely enough that the official SQLite documentation
doubles as its own reference. `ksqlite-kapi` doesn't have that shortcut, so this README covers it
section by section instead, each with runnable snippets. Once a type or function's exact signature
is needed rather than an explanation, the generated API documentation answers that faster.

Some types used here without much explanation, mostly enums and flags modeling a finite set of
values SQLite itself defines (`SqliteOpenFlag`, `SqliteDataType`, the cipher types used in
[Encryption](#encryption), and so on), live in [`ksqlite-types`](../ksqlite-types/README.md) and
are shared as-is with `ksqlite-capi`. Its README is worth a look whenever one shows up here
unexplained.

## Table of contents

- [Getting started](#getting-started)
- [Conventions](#conventions)
    - [Errors](#errors)
    - [Closing resources](#closing-resources)
    - [Default values](#default-values)
- [Static SQLite APIs](#static-sqlite-apis)
- [Database connections](#database-connections)
- [Prepared statements](#prepared-statements)
- [Values](#values)
- [Transactions](#transactions)
- [Application-defined functions](#application-defined-functions)
    - [Scalar functions](#scalar-functions)
    - [Aggregate functions](#aggregate-functions)
    - [Window functions](#window-functions)
    - [Auxiliary data](#auxiliary-data)
- [Hooks](#hooks)
- [Blobs](#blobs)
- [Backup](#backup)
- [Serialization](#serialization)
- [Snapshots](#snapshots)
- [Write-ahead log](#write-ahead-log)
- [Buffers](#buffers)
- [Virtual tables](#virtual-tables)
- [Virtual file systems](#virtual-file-systems)
- [Encryption](#encryption)
    - [Using a builtin cipher](#using-a-builtin-cipher)
    - [Writing a custom cipher](#writing-a-custom-cipher)
- [Configuration](#configuration)

## Getting started

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.manriif.ksqlite:ksqlite-kapi:<version>")
        }
    }
}
```

[`SQLite`](src/commonMain/kotlin/ksqlite/kapi/SQLite.kt) is the entry point. Initializing it
initializes SQLite itself, and returns the object every other API in this module hangs off of,
directly or indirectly:

```kotlin
val sqlite = SQLite.initialize()
val db = sqlite.open(":memory:")

db.execute("CREATE TABLE fruits(name TEXT NOT NULL);")

db.prepare("INSERT INTO fruits VALUES (?);").use { insert ->
    insert.parameters.bindString(1, "Kiwi")
    insert.step()
}

db.prepare("SELECT name FROM fruits;").use { select ->
    var row = select.step()

    while (row != null) {
        println(row.getString(0))
        row = select.step()
    }
}

db.close()
sqlite.close()
```

Only one `SQLite` instance can exist at a time, for the whole process. Every resource opened through
this module (a connection, a statement, and so on) is tied to that one instance. 
`SQLite.initialize()` throws `IllegalStateException` if a previous instance was returned and not yet
closed. It also takes an optional trailing lambda for options that can only be set before SQLite 
starts up, threading mode among them, see [Configuration](#configuration).

Closing `SQLite` shuts SQLite down process-wide, so every connection, statement, and other
resource opened through it must be closed first. Closing a connection closes everything opened
through that connection in turn (statements, blobs, and so on), but `SQLite.close()` itself does
not reach into every connection and close them for you. It's cleanup all the way down, and it's
the caller's job to walk it, the same way `sqlite.close()` above only works because `db` (and
everything opened from `db`) was already closed first.

## Conventions

### Errors

Almost every call in this module can throw. `SQLiteException` carries the SQLite result code
(`SQLiteException.result`) and an error message, wherever the underlying SQLite call can fail.
It's a `RuntimeException`, not a checked exception, so nothing forces a `try`/`catch` at the call
site, but most real code ends up with one somewhere.

```kotlin
try {
    db.execute("INSERT INTO does_not_exist VALUES ('Kiwi');")
} catch (e: SQLiteException) {
    println("${e.result}: ${e.message}")
}
```

`db.lastError` (see [Database connections](#database-connections)) exposes the same information
`SQLiteException` was built from, message, code, extended code, byte offset, without needing a
`catch` block. Useful for the rare case where a result is inspected without throwing first.

### Closing resources

Every resource with a lifecycle, a `SQLite` instance, a `DatabaseConnection`, a `PreparedStatement`,
a `Blob`, a `Backup`, a `Snapshot`, a `Buffer`, a `Registration`, implements `AutoCloseable`.
`use { }`, from the Kotlin standard library, closes one automatically once the block returns or
throws:

```kotlin
db.prepare("SELECT name FROM fruits;").use { select ->
    // select is closed once this block exits, even if it throws
}
```

Closing is idempotent, calling `close()` twice is not an error. Using a resource after it's closed
throws `IllegalStateException` instead of silently doing nothing or crashing, the same as trying
to use a connection, or any of its statements, after that connection was closed.

### Default values

Where a parameter has an obvious default, `ksqlite-kapi` supplies one, unlike `ksqlite-capi`. The
main database is assumed wherever a `database` parameter defaults to `null` or `"main"`, and most
flags default to their most common value, `SqliteBlobOpenFlag.READONLY` for `openBlob()`'s `flags`
for instance. Every default is documented on the parameter itself, in the generated API
documentation, so it's worth a quick look before assuming what a bare call does.

## Static SQLite APIs

A handful of SQLite facts and utilities don't need SQLite to be initialized at all, they're
reachable straight off the `SQLite` companion object, before `SQLite.initialize()` is ever called:

```kotlin
println(SQLite.version)       // "3.51.0", for example
println(SQLite.versionNumber) // 3051000
println(SQLite.sourceId)      // the SQLite check-in id this build was compiled from
println(SQLite.multipleCiphersVersion)

println(SQLite.isThreadSafe)
println(SQLite.compileOptions) // e.g. ["ENABLE_FTS5", "THREADSAFE=1", ...], SQLITE_ prefix omitted
println(SQLite.keywordCount)
println(SQLite.isKeyword("SELECT")) // true
```

`SQLite.matchGlob(pattern, input)` and `SQLite.matchLike(pattern, input, escape)` run SQLite's own
`GLOB`/`LIKE` matching outside of a query, useful for filtering in Kotlin using the exact same
rules a `WHERE` clause would:

```kotlin
if (SQLite.matchGlob("*.db", fileName)) {
    // ...
}
```

`SQLite.caseIndependentComparator` is a `Comparator<String>` that compares text the same way
SQLite's default `NOCASE`-independent identifier comparison does, useful for sorting or comparing
table/column names the way SQLite itself would consider them equal.
`SQLite.createCaseIndependentComparator(maxBytes)` returns one that only looks at the first
`maxBytes` of each string.

`SQLite.log(errorCode, message)` writes to whatever `Logger` is currently configured, see
[Configuration](#configuration). `SQLite.isCompleteSqlStatement(sql)` reports whether `sql` looks
like a complete statement, useful for a REPL that needs to know whether to keep reading more input
before handing a multi-line statement to `prepare()`/`execute()`.

## Database connections

[`DatabaseConnection`](src/commonMain/kotlin/ksqlite/kapi/connection/DatabaseConnection.kt) wraps a
single SQLite connection, obtained from `SQLite.open()`. It's the object every other API in this
section, and most of the sections after it, hangs off of.

```kotlin
val db = sqlite.open(
    fileName = "app.db",
    flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.CREATE
)
```

`fileName` can be `:memory:` for a private, in-memory database, a real file path, or a
[SQLite URI](https://sqlite.org/uri.html) (`file:app.db?mode=rwc&cache=shared`) for finer control.
A URI's query parameters can carry custom options a VFS reads back through
[`FileName`](#virtual-file-systems). Nothing stops opening more than one connection, to the same
database or different ones, at the same time, each an independent `DatabaseConnection`.

`db.execute(sql)` runs one or more semicolon-separated statements straight through, optionally
with a callback invoked once per result row:

```kotlin
db.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY, name TEXT NOT NULL);")

db.execute("SELECT id, name FROM fruits;") { columnCount, columnValues, columnNames ->
    println(columnValues.joinToString())
    false // true aborts the statement early
}
```

`execute()` is convenient for schema changes, one-off statements, and anything where the result
columns are read as text. It re-parses the SQL every time it runs, and every result is a
`String?`. [Prepared statements](#prepared-statements) avoid both, and are the only way to bind
parameters, so reach for them instead of building SQL strings by hand with user-provided values.

`db.config` exposes the same per-connection toggles as `sqlite3_db_config()`, as regular Kotlin
properties and functions instead of a variadic call:

```kotlin
db.config.isForeignKeyEnabled = true
db.config.floatingPointDigits = 15
```

A few other members of `DatabaseConnection` are worth knowing about up front, each covered in more
detail in its own section further down:

- `db.lastError`, see [Errors](#errors)
- `db.fileControl`, a typed wrapper over `sqlite3_file_control()`, for VFS-specific options
- `db.tableColumnMetadata(table, column)`, the declared type, collation, and constraints of one
  column, without querying `PRAGMA table_info`
- `db.getStatus(option)` and `db.releaseMemory()`, memory accounting and pressure relief
- `db.wal`, see [Write-ahead log](#write-ahead-log)
- `db.cipherConfig`/`db.cipherData`, see [Encryption](#encryption)

## Prepared statements

`db.prepare(sql)` compiles a statement once, returning a
[`PreparedStatement`](src/commonMain/kotlin/ksqlite/kapi/statement/PreparedStatement.kt) that can
be bound and stepped repeatedly, without re-parsing the SQL or re-planning the query each time:

```kotlin
db.prepare("INSERT INTO fruits(name) VALUES (?);").use { insert ->
    for (name in listOf("Kiwi", "Mango", "Banana")) {
        insert.parameters.bindString(1, name)
        insert.step()
        insert.reset()
    }
}
```

### Binding parameters

`insert.parameters` exposes one `bind*()` method per SQLite type, indexed from `1`, the same
one-based indexing SQLite itself uses:

```kotlin
statement.parameters.bindInt(1, 42)                 // INTEGER
statement.parameters.bindDouble(2, 4.2)             // REAL
statement.parameters.bindString(3, "hello")         // TEXT
statement.parameters.bindByteArray(4, byteArrayOf(1, 2, 3)) // BLOB
statement.parameters.bindNull(5)                    // NULL
```

Every scalar `bind*()` also has a nullable counterpart accepting `Int?`, `String?`, and so on
directly, binding `NULL` automatically instead of requiring a separate check:

```kotlin
val name: String? = null
statement.parameters.bindString(1, name) // binds NULL, instead of throwing on the non-nullable overload
```

`bindBoolean(index, value)` binds `1`/`0`, SQLite's own convention for booleans, there's no
dedicated storage class for one. It's plain sugar over `bindInt()`, nothing prevents binding `2`
or any other integer directly instead.

Named parameters (`:name`, `@name`, `$name`) resolve their index through
`parameters.getIndex(name)` before binding:

```kotlin
val statement = db.prepare("SELECT * FROM fruits WHERE name = :name;")
statement.parameters.bindString(statement.parameters.getIndex(":name"), "Kiwi")
```

`bindZeroBlob(index, size)` binds a placeholder blob of `size` bytes, all set to zero, instead of
real content (an `Int` overload and a `Long` one for larger sizes). It reserves the space without
SQLite ever holding the whole value in memory at once, exactly the kind of large-content write
[`Blob`](#blobs) exists to stream piecemeal. The usual reason to bind a zero-blob instead of a real
one is to open it as a `Blob` right after inserting it, and write the actual content in chunks:

```kotlin
db.prepare("INSERT INTO fruits(name, image) VALUES (?, ?);").use { insert ->
    insert.parameters.bindString(1, "Kiwi")
    insert.parameters.bindZeroBlob(2, size = imageBytes.size) // reserves the space, writes nothing yet
    insert.step()
}

db.openBlob("fruits", "image", db.lastInsertRowid, flags = SqliteBlobOpenFlag.READWRITE).use { blob ->
    blob.write(imageBytes) // fills in the space reserved above, in as many chunks as needed
}
```

Large or binary content can also be bound directly from a [`Buffer`](#buffers) instead of a
`ByteArray`, with a cleanup callback invoked once SQLite is done with it:

```kotlin
val buffer = Buffer.allocate(imageBytes.size)
buffer.write(imageBytes)

statement.parameters.bindBuffer(4, buffer) { closed ->
    closed.close()
}
```

`parameters.clear()` resets every bound parameter back to `NULL`, keeping the statement itself
compiled and ready to reuse.

### Reading rows

`step()` returns a [`Row`](src/commonMain/kotlin/ksqlite/kapi/statement/Row.kt) for as long as
there's data left, or `null` once the statement is exhausted. `forEachRow { }` wraps that loop:

```kotlin
db.prepare("SELECT id, name FROM fruits WHERE name LIKE ?;").use { select ->
    select.parameters.bindString(1, "%an%")

    select.forEachRow { row ->
        val id = row.getLong(0)
        val name = row.getString(1)
        println("$id: $name")
    }
}
```

`Row` has one `get*(index)` per type, also zero-based like `bind*()` is one-based: `getInt()`,
`getLong()`, `getDouble()`, `getString()`, `getByteArray()` (a `ByteArray?`), `getBuffer()` (a
[`ReadableBuffer?`](#buffers), a zero-copy view instead of a fresh `ByteArray`), and `getValue()`
(an [`UnprotectedValue`](#values), for passing a column's content elsewhere without going through a
Kotlin type at all). SQLite converts between types on demand the same way it does everywhere else,
`getString()` on an `INTEGER` column returns its text representation, for instance.
`row.getType(index)` reports the column's actual `SqliteDataType` if that matters,
`row.getColumnName(index)` its name.

`getBoolean(index)` reads `getInt(index)` back as `1`/`0`, the same convention `bindBoolean()`
writes. Unlike the type conversions above, it doesn't coerce, any value other than `1` or `0`
throws `SQLiteException`, on the theory that a column meant to hold a boolean should actually
hold one.

A `Row` is only valid until the next `step()`, a `reset()`, or the statement's own `close()`.
Reading from a stale one throws `IllegalStateException`, the same as any other closed resource, so
don't hold on to a `Row` past the next `step()` call.

`reset()` rewinds a statement to be re-executed without recompiling it, keeping its bound
parameters, as the insert loop at the top of this section does.

## Values

`ksqlite-kapi` never exposes a raw SQLite value as a Kotlin primitive directly where SQLite itself
hands over a pointer to native memory it owns. Instead, one of three
[`Value`](src/commonMain/kotlin/ksqlite/kapi/value/Value.kt) subtypes shows up, depending on where
it came from, and it's worth knowing which is which before running into `IllegalStateException` or
a type that seemingly has no `getAsInt()`.

**`ProtectedValue`** is fully readable, `getAsInt()`, `getAsLong()`, `getAsDouble()`,
`getAsString()`, `getAsByteArray()`, `getAsBuffer()`, `getAsBoolean()` (`1`/`0` only, anything else
throws `SQLiteException`, see [Reading rows](#reading-rows)), plus `type` (its `SqliteDataType`)
and a few less common properties (`isFromBind`, `subtype`, `encoding`). This is what arrives as the
`arguments` of a [scalar/aggregate/window function](#application-defined-functions), and as
`PreupdateHookScope.oldValue()`/`newValue()`. It's only valid for the duration of the call it was
handed to, reading it afterward throws `IllegalStateException`, same as any other resource used
past its lifetime.

**`UnprotectedValue`** is what `Row.getValue(index)` returns. It has no `getAsInt()` or similar,
the only thing it supports is being handed straight back to SQLite, as a bind parameter
(`parameters.bindValue(index, value)`) or a function result (`resultValue(value)`, see
[Application-defined functions](#application-defined-functions)), without decoding it into a
Kotlin type at all:

```kotlin
db.prepare("SELECT name FROM fruits WHERE id = 1;").use { select ->
    val name = select.step()!!.getValue(0) // an UnprotectedValue, valid only while select stays open

    db.prepare("INSERT INTO archived_fruits(name) VALUES (?);").use { insert ->
        insert.parameters.bindValue(1, name) // copies the value directly, no round trip through Kotlin
        insert.step()
    }
}
```

Trying to read an `UnprotectedValue`'s content directly isn't possible, since the type itself
doesn't expose it. Call `getInt()`/`getString()`/... on the `Row` instead if the actual content is
needed as a Kotlin value.

**`DuplicatedValue`**, obtained by calling `.duplicate()` on any `Value` (protected or
unprotected), copies the underlying SQLite value into memory this module owns. Unlike the other
two, it outlives the call it was created in, useful for holding on to a value past a function call
or a row, an aggregate function accumulating values across `step()` calls, for instance. It's a
`ProtectedValue`, fully readable, but now something to close by hand, through `.close()`, once no
longer needed, since nothing else will:

```kotlin
val saved = arguments[0].duplicate()
// ... later, in a different call ...
println(saved.getAsInt())
saved.close()
```

## Transactions

There's no dedicated transaction API. A transaction is plain SQL, the same as with any other
SQLite driver:

```kotlin
db.execute("BEGIN;")

try {
    db.prepare("INSERT INTO fruits(name) VALUES (?);").use { insert ->
        insert.parameters.bindString(1, "Kiwi")
        insert.step()
    }

    db.execute("COMMIT;")
} catch (e: SQLiteException) {
    db.execute("ROLLBACK;")
    throw e
}
```

`db.isAutocommit` reports whether a transaction is currently open (`false` means one is).
`db.getTransactionState(schema)` gives a finer-grained read, none, read, or write, for a specific
attached database, `null` for the main one.

## Application-defined functions

SQL functions implemented in Kotlin, callable from any SQL statement once registered, the same
way SQLite's own built-in functions (`length()`, `abs()`, and so on) are. Three kinds exist,
depending on how many rows a call sees at once.

### Scalar functions

[`ScalarFunction`](src/commonMain/kotlin/ksqlite/kapi/function/ScalarFunction.kt) computes one
result from the arguments of a single call, registered through
`db.createFunction(name, argumentCount, encoding, fn)`. An overload without `encoding` is also
available, using `SqliteTextEncoding.UTF8`, the only one currently supported, so it's rarely
spelled out:

```kotlin
db.createFunction("double", 1, ScalarFunction { arguments ->
    resultInt(arguments[0].getAsInt() * 2)
})

db.execute("SELECT double(21);") // 42
```

`arguments` is an `Array<ProtectedValue>`, see [Values](#values). `result*(...)` reports the value
back to SQLite and must be the function body's return value, it comes from the `ResultScope`
receiver every function body runs in, has one method per SQLite type plus `resultBoolean(value)`
(`1`/`0` sugar, the same as `bindBoolean()`), the same shape as `PreparedStatementParameters.bind*()`,
and returns `Result`, a marker proving one of them was actually called. `resultError(message)`,
also on that receiver, fails the call instead, equivalent to throwing `SQLiteException` from inside
the function body, which works too.

`createFunction(...)` returns a [`Registration`](src/commonMain/kotlin/ksqlite/kapi/connection/Registration.kt),
closing it unregisters the function, the same way for all three kinds below, and for
`createCollation(...)`/`createModule(...)`, see [Hooks](#hooks) and
[Virtual tables](#virtual-tables):

```kotlin
val registered = db.createFunction("double", 1, ScalarFunction { arguments ->
    resultInt(arguments[0].getAsInt() * 2)
})

// ...

registered.close()
```

Registering another function under the same name, argument count, and encoding replaces this one,
and closing the handle for the one that got replaced is then a no-op, it has nothing left to
unregister. Closing a handle twice is a no-op for the same reason `use { }`/`AutoCloseable` is
idempotent everywhere else in this module, see [Closing resources](#closing-resources).

### Aggregate functions

[`AggregateFunction`](src/commonMain/kotlin/ksqlite/kapi/function/AggregateFunction.kt) computes
one result across every row a `GROUP BY` (or the whole result set, without one) feeds it, the same
shape as `sum()` or `count()`:

```kotlin
val product = object : AggregateFunction {
    override fun AggregateFunctionStepScope.step(arguments: Array<ProtectedValue>) {
        val accumulator = getOrCreateAggregateContext { intArrayOf(1) }
        accumulator[0] *= arguments[0].getAsInt()
    }

    override fun AggregateFunctionFinalScope.final() =
        resultInt(getContextOrNull<IntArray>()?.get(0) ?: 1)
}

db.createFunction("product", 1, product)
db.execute("SELECT product(quantity) FROM fruits;")
```

`step()` runs once per row. `getOrCreateAggregateContext { ... }` returns whatever mutable state
the aggregation needs, an `IntArray` of one element above, computed once on the first row and
reused on every following one for the same group. `final()` runs once, after every row in the
group was seen, to produce the actual result. `getContextOrNull<C>()` reads that same state back
without creating it, `null` if `step()` was never called for this group, an empty table for
instance.

### Window functions

[`WindowFunction`](src/commonMain/kotlin/ksqlite/kapi/function/WindowFunction.kt) extends
`AggregateFunction` with two more methods, `inverse()` and `value()`, needed to slide a window
across rows (an `OVER (... ROWS BETWEEN ...)` clause) without recomputing the aggregate from
scratch for every row:

```kotlin
val runningTotal = object : WindowFunction {
    override fun AggregateFunctionStepScope.step(arguments: Array<ProtectedValue>) {
        val total = getOrCreateAggregateContext { intArrayOf(0) }
        total[0] += arguments[0].getAsInt()
    }

    override fun WindowFunctionInverseScope.inverse(arguments: Array<ProtectedValue>) {
        val total = getAggregateContextOrNull<IntArray>()
        total?.let { it[0] -= arguments[0].getAsInt() }
    }

    override fun AggregateFunctionFinalScope.final() =
        resultInt(getContextOrNull<IntArray>()?.get(0) ?: 0)

    override fun AggregateFunctionFinalScope.value() =
        resultInt(getContextOrNull<IntArray>()?.get(0) ?: 0)
}

db.createFunction("running_total", 1, runningTotal)
```

`step()` adds a row entering the window, the same as for a plain aggregate. `inverse()` removes a
row leaving it, as the window slides forward, undoing what `step()` did for that row. `value()`
reports the current result without ending the aggregation, called after every row, while `final()`
still runs once at the very end, exactly like a plain `AggregateFunction`.

### Auxiliary data

A scalar function's `ScalarFunctionFuncScope` (and a window function's step/inverse scopes) also
expose `AuxDataScope`, a per-argument cache SQLite itself manages. It's kept across calls as long as
SQLite can prove the argument at that position hasn't changed between rows, a `LIKE` pattern that's
the same literal on every row of a scan, for instance. It's discarded otherwise. Useful for caching
something expensive to compute from a constant argument, a compiled regular expression:

```kotlin
db.createFunction("regex_match", 2, ScalarFunction { arguments ->
    val pattern = getOrCreateAuxData(0) { Regex(arguments[0].getAsString()!!) }
    val matches = pattern.containsMatchIn(arguments[1].getAsString().orEmpty())
    resultInt(if (matches) 1 else 0) // SQLite has no dedicated BOOLEAN type, 0/1 is its own convention
})
```

`getOrCreateAuxData(index) { ... }` computes and caches the value the first time, and returns the
cached one on later calls where SQLite decided to keep it. There's no guarantee it's kept at all,
this is an optimization, not a correctness mechanism, the compute lambda must always be safe to
run again.

## Hooks

SQLite's global and per-connection hooks are exposed as plain Kotlin functional interfaces,
registered on `SQLite` or `DatabaseConnection`. Passing `null` clears a previously set hook.

The authorizer is a representative example, called during `prepare()`/`execute()` for every action
a statement would take, and able to allow, deny, or ignore each one:

```kotlin
db.setAuthorizer { action, tableName, _, _, _ ->
    if (action == SqliteActionCode.DROP_TABLE) {
        SqliteAuthorizerStatus.DENY
    } else {
        SqliteAuthorizerStatus.OK
    }
}
```

The update hook fires after a row was actually inserted, updated, or deleted, useful for cache
invalidation or change notifications:

```kotlin
db.setUpdateHook { action, databaseName, tableName, rowid ->
    println("$action on $databaseName.$tableName, rowid $rowid")
}
```

The preupdate hook fires before the change happens instead, with access to both the old and new
column values, `oldValue()`/`newValue()` throwing `SQLiteException` for whichever side doesn't
apply (there's no "old" row on an `INSERT`, no "new" row on a `DELETE`):

```kotlin
db.setPreupdateHook { connection, action, databaseName, tableName, oldRowid, newRowid ->
    if (action == SqliteActionCode.UPDATE) {
        println("column 0 changing from ${oldValue(0).getAsString()} to ${newValue(0).getAsString()}")
    }
}
```

Trace reports statement lifecycle events, which ones depends on the `SqliteTraceEventCode` mask
passed to `setTrace()`. `or` combines more than one, the same way SQLite's own bitmask does, and
`event` reports which one actually fired for a given callback invocation:

```kotlin
db.setTrace(SqliteTraceEventCode.STMT or SqliteTraceEventCode.PROFILE) { event ->
    when (event) {
        is TraceEvent.Stmt -> println("about to run: ${event.sql}")
        is TraceEvent.Profile -> println("${event.statement.sql} took ${event.nanos / 1_000_000}ms")
        else -> Unit // ROW/CLOSE, not requested above, won't actually reach this callback
    }
}
```

The rest follow the same shape, a registration function taking a functional interface, documented
on `DatabaseConnection` and `SQLite` themselves:

| Registration                                                | Callback                        | Fires                                               |
|-------------------------------------------------------------|---------------------------------|-----------------------------------------------------|
| `SQLite.addAutoExtension`                                   | `AutoExtension`                 | once per new connection, before it's returned       |
| `DatabaseConnection.setBusyHandler` / `setBusyTimeout`      | `BusyHandler`                   | a table is locked by another connection             |
| `DatabaseConnection.createCollation` / `setCollationNeeded` | `Collation` / `CollationNeeded` | comparing text with a custom or missing collation   |
| `DatabaseConnection.setCommitHook` / `setRollbackHook`      | `CommitHook` / `RollbackHook`   | a transaction is about to commit or was rolled back |
| `DatabaseConnection.setProgressHandler`                     | `ProgressHandler`               | periodically during a long-running statement        |
| `DatabaseConnection.setAutovacuumPages`                     | `AutovacuumPages`               | before each incremental autovacuum                  |
| `DatabaseConnection.wal.setHook`                            | `WriteAheadLogHook`             | data is committed to a database in WAL mode         |

`createCollation` is the one entry above that isn't a plain setter, it returns a `Registration`
the same way `createFunction`/`createModule` do, see [Scalar functions](#scalar-functions), and it
has the same UTF-8-only overload too. `setCollationNeeded` stays a plain callback registration,
clearing it back to `null` is the only way to unregister it.

## Blobs

[`Blob`](src/commonMain/kotlin/ksqlite/kapi/blob/Blob.kt) streams a single column's content in and
out without loading the whole row, useful for large `BLOB` columns, images or file content stored
in the database instead of on disk, that shouldn't be read or written as one giant `ByteArray`:

```kotlin
db.openBlob("fruits", "image", rowid).use { blob ->
    val chunk = ByteArray(4096)
    blob.read(chunk)
    blob.write(chunk, offset = 4096)
}
```

`blob.bytes` is the column's total size. `read()`/`write()` both take an `offset` into the blob's
content, defaulting to `0`, to move through it a chunk at a time instead of all at once.
`blob.reopen(rowid)` moves an already-open blob to a different row of the same table and column,
cheaper than closing and reopening one when iterating over many rows.

The column has to already exist, `openBlob()` doesn't create a row. Insert a placeholder first,
`zeroblob(n)` in SQL or `bindZeroBlob(index, size = n)` from a prepared statement, see
[Binding parameters](#binding-parameters), for a blob of `n` bytes to then open and fill in.

## Backup

[`Backup`](src/commonMain/kotlin/ksqlite/kapi/backup/Backup.kt) copies one database into another,
live, page by page, safe to run while the source database is being written to concurrently:

```kotlin
val source = sqlite.open("app.db")
val destination = sqlite.open("app-backup.db")

Backup.create(destination, source).use { backup ->
    do {
        backup.step(5) // copies up to 5 pages at a time
    } while (backup.remaining > 0)
}

destination.close()
source.close()
```

`step(-1)` copies every remaining page in one call instead of stepping in batches. Stepping in
small batches, as above, keeps the source database usable by other connections between steps,
useful for backing up a large, actively-used database without locking it out for the whole
duration. `backup.pageCount`/`backup.remaining` report progress, the total page count as of the
last `step()` and how many are left.

`Backup.create(destination, destinationName, source, sourceName)`, the other overload, backs up an
attached database instead of the main one on either side.

## Serialization

`db.serialize()` returns the whole content of a database as an in-memory
[`Buffer`](#buffers), and `db.deserialize(buffer)` loads one back, useful for cloning a database
in memory, or shipping one over the network or into another process without going through a file:

```kotlin
when (val result = db.serialize()) {
    is SerializeResult.Mutable -> {
        val buffer = result.buffer // a Buffer, this side owns it and must close it once done

        val copy = sqlite.open(":memory:")
        copy.deserialize(buffer, databaseSize = buffer.byteSize)
    }
    is SerializeResult.Immutable -> {
        val buffer = result.buffer // a ReadableBuffer, viewing SQLite's own in-memory database
        // read-only: there's no close() to call on a ReadableBuffer in the first place, and no
        // Buffer to hand to deserialize() either, only a real copy (the Mutable case) can be
    }
    is SerializeResult.Failure -> error("Serialization failed")
}
```

`SerializeResult` is `Mutable` unless `SqliteSerializeFlag.NOCOPY` was passed to `serialize()`, in
which case SQLite hands back a direct, read-only view over its own in-memory database instead of
copying it (`Immutable`), or fails if the database isn't already the right shape for that
(`Failure`). `deserialize()` similarly accepts `SqliteDeserializeFlag.READONLY` and `FREEONCLOSE`,
the latter handing ownership of `buffer` to SQLite, to be freed once the connection closes instead
of by the caller.

## Snapshots

[`Snapshot`](src/commonMain/kotlin/ksqlite/kapi/snapshot/Snapshot.kt) captures a point-in-time
view of a database in WAL mode, for a connection to read from later regardless of writes that
happen in between, other connections included:

```kotlin
db.execute("PRAGMA journal_mode=WAL;")

db.execute("BEGIN;")
val snapshot = db.createSnapshot() // main database by default
db.execute("COMMIT;")

// ... later, possibly after other connections wrote to the database ...

db.execute("BEGIN;")
db.openSnapshot(snapshot)
// reads in this transaction now see the database exactly as it was at the snapshot
db.execute("COMMIT;")

snapshot.close()
```

`createSnapshot()` only succeeds right after `BEGIN`, before any statement has actually read from
the database in that transaction. Calling it anywhere else throws `SQLiteException`, `openSnapshot()`
has the same restriction. This is SQLite's own contract for snapshots, not something this module
hides behind a friendlier API, so `BEGIN` immediately followed by `createSnapshot()`/`openSnapshot()`
is the pattern to reach for.

Snapshots compare with each other through `Comparable`, `snapshot1 < snapshot2` reports which one
is older. `db.recoverSnapshots()` makes snapshots taken by other connections, before this one was
opened, available to `openSnapshot()`.

## Write-ahead log

`db.wal` exposes WAL-specific operations: `autoCheckpoint(frameCount)` sets how many WAL frames
accumulate before SQLite checkpoints automatically after a commit, `checkpoint(mode, database)`
runs one on demand, and `setHook(hook)` (see [Hooks](#hooks)) is notified every time data is
committed to a database in WAL mode.

```kotlin
val result = db.wal.checkpoint(SqliteCheckpointMode.TRUNCATE)
println("${result.checkpointedFrameCount}/${result.frameCount} frames checkpointed")
```

`SqliteCheckpointMode.PASSIVE`, the default, checkpoints as much as possible without blocking
other connections. `FULL`, `RESTART`, and `TRUNCATE` block increasingly more to guarantee more, see
the type's own documentation for the exact difference between them.

## Buffers

[`Buffer`](src/commonMain/kotlin/ksqlite/kapi/buffer/Buffer.kt) is a block of native memory holding
`ByteArray`-shaped content. Blob content passed to `PreparedStatementParameters.bind*()` or
`ResultScope.result*()`, and the result of `serialize()`, all move through it.

```kotlin
val buffer = Buffer.allocate(1024)
buffer.write(payload)

val readBack = ByteArray(1024)
buffer.read(readBack)

buffer.resize(2048) // grows or shrinks in place, content up to the smaller of the two sizes kept

buffer.close()
```

`write()`/`read()` both take an optional `size`, `sourceOffset`, and `destinationOffset`, to move a
chunk at a time instead of the whole buffer, the same shape as `Blob.read()`/`write()`.

A `Buffer` still referenced by SQLite, as a live bind parameter that hasn't been cleared or
finished with yet, for instance, throws `BufferInUseException` if closed, resized, or written to
in the meantime:

```kotlin
statement.parameters.bindBuffer(1, buffer)

buffer.close() // throws BufferInUseException, buffer is still bound

statement.parameters.clear() // releases SQLite's reference on buffer
buffer.close() // works now
```

Reading from it (`read()`, not `write()`) while it's referenced is fine, only mutating or freeing
it is restricted, to stop the content SQLite is currently using from shifting or disappearing out
from under it.

> [!NOTE]
> `write()`/`read()` still copy bytes across the Kotlin heap/native memory boundary on every
> platform this module targets, so `Buffer` is not a zero-copy alternative to `ByteArray`. What it
> buys instead is avoiding repeat copies: the same native allocation can be filled once and handed
> to SQLite directly, or read back a chunk at a time instead of materializing a full `ByteArray` up
> front. `ReadableBuffer`, `Buffer`'s read-only counterpart, is the one place this module gives out 
> something genuinely zero-copy: `Row.getBuffer()` and `Value.getAsBuffer()` return a view directly 
> over memory SQLite itself owns, valid only as long as the row or value it came from is, and 
> obtaining that view moves no bytes at all. Copying its content into a `ByteArray`, by calling 
> `read()` on it, still costs the same copy as everywhere else.

## Virtual tables

A virtual table backs SQL rows with whatever storage or logic an application wants, a table that
isn't really a table, implemented as plain Kotlin classes instead of a struct of function
pointers. [`VirtualTableModule`](src/commonMain/kotlin/ksqlite/kapi/vtab/VirtualTableModule.kt)
creates or connects to one, returning a
[`VirtualTable`](src/commonMain/kotlin/ksqlite/kapi/vtab/VirtualTable.kt) that answers queries
through a [`VirtualTableCursor`](src/commonMain/kotlin/ksqlite/kapi/vtab/VirtualTableCursor.kt).

The cursor walks the rows a query needs to see, one at a time:

```kotlin
class MemoryCursor(val table: MemoryTable) : VirtualTableCursor() {
    var position = 0

    override fun eof() = position >= table.rows.size

    override fun VirtualTableFilterScope.filter(
        idxNum: Int,
        idxStr: String?,
        arguments: Array<ProtectedValue>
    ) {
        position = 0 // a real implementation would use idxNum/arguments to narrow the scan
    }

    override fun next() {
        position++
    }

    override fun VirtualTableColumnScope.column(index: Int) =
        resultString(table.rows[position][index])

    override fun rowid() = position.toLong()
    override fun close() = Unit
}
```

`filter()` starts a scan, `next()` advances it, `eof()` reports whether it's exhausted, and
`column()`/`rowid()` report the current row's content. `idxNum`/`idxStr`/`arguments` in `filter()` 
come from whatever `bestIndex()`, on the table itself, chose for this particular query, see below.

The table owns the cursor's underlying data, and describes itself to SQLite once, when a
connection first uses it:

```kotlin
class MemoryTable : VirtualTable() {
    val rows = mutableListOf<List<String>>()

    override fun VirtualTableBestIndexScope.bestIndex(info: SqliteIndexInfo) {
        // a real implementation inspects info's constraints and picks a strategy accordingly,
        // this one always does a full scan
        info.estimatedRows = rows.size.toLong()
    }

    override fun disconnect() = Unit
    override fun destroy() = Unit
    override fun open(): VirtualTableCursor = MemoryCursor(this)
}
```

`bestIndex()` is called by the query planner to ask "given these constraints (a `WHERE` clause, an
`ORDER BY`), how would you scan yourself, and how expensive would it be?" It's the one mandatory
method with no reasonable default, a table has to at least report an estimated cost, even a fake
one, but doesn't have to actually use any constraint if it doesn't want to, it just won't be as
fast as it could be. `disconnect()`/`destroy()` both release a table (the difference is whether
the backing store should be destroyed too, `destroy()` is `DROP TABLE`, `disconnect()` is just
closing the connection), and `open()` creates a fresh cursor for a new scan.

Registering the module ties a name to it:

```kotlin
db.createModule("memory_table", module = object : VirtualTableModule.EponymousOnly() {
    override fun VirtualTableCreateOrConnectScope.connect(
        connection: DatabaseConnection,
        arguments: Array<String>
    ): VirtualTable {
        declare("CREATE TABLE x(name TEXT)")
        return MemoryTable()
    }
})

db.execute("SELECT name FROM memory_table;")
```

`connect()` (or `create()`, see below) runs once per table instance, and must call `declare(sql)`
to describe the table's columns to SQLite before returning the `VirtualTable`. `arguments` carries
whatever was passed after the module name, `CREATE VIRTUAL TABLE t USING memory_table(a, b, c)`
hands `["memory_table", "main", "t", "a", "b", "c"]` to `connect()`/`create()`, the module name,
schema, and table name always come first.

`bestIndex()`, `disconnect()`, `destroy()`, and `open()`, together with the cursor's `eof()`,
`filter()`, `next()`, `column()`, `rowid()`, and `close()`, are the minimum needed for any table,
even a read-only one, like the example above. Everything else on `VirtualTable`, `update()` for
writes, `begin()`/`sync()`/`commit()`/`rollback()`/`savepoint()`/`release()`/`rollbackTo()` for
transactions, `rename()`, `findFunction()`, and `integrity()`, is meant to be opted into, so SQLite
only calls the ones a module declared support for.

Opting in happens through the `optionalFunctions` constructor parameter every `VirtualTableModule`
subclass accepts, a `Set<VirtualTableOptionalFunction>` listing which of those methods the tables
this module produces actually override:

```kotlin
db.createModule("memory_table", module = object : VirtualTableModule.EponymousOnly(
    optionalFunctions = setOf(VirtualTableOptionalFunction.Update)
) {
    override fun VirtualTableCreateOrConnectScope.connect(
        connection: DatabaseConnection,
        arguments: Array<String>
    ): VirtualTable {
        declare("CREATE TABLE x(name TEXT)")
        return MemoryTable() // now overrides update(), see below
    }
})
```

SQLite decides which callbacks exist for a module as a whole, once, before any table is created or
connected to, so `optionalFunctions` has to be declared upfront this way rather than discovered
from a `VirtualTable` instance later. A function listed here that the produced `VirtualTable`
doesn't actually override throws `VirtualTableOptionalFunctionNotImplementedError` the first time
SQLite tries to call it, instead of silently doing nothing.

```kotlin
override fun VirtualTableUpdateScope.update(arguments: Array<ProtectedValue>): Long? {
    return when {
        // a single argument means DELETE, it holds the rowid to remove
        arguments.size == 1 -> {
            rows.removeAt(arguments[0].getAsLong().toInt())
            null
        }

        // arguments[0] is NULL for INSERT, arguments[1] is the proposed rowid (NULL = auto-assign)
        arguments[0].type == SqliteDataType.NULL -> {
            rows.add(listOf(arguments[2].getAsString()!!))
            (rows.size - 1).toLong()
        }

        // otherwise, it's an UPDATE: arguments[0] is the existing rowid, arguments[1] the new one
        else -> {
            rows[arguments[0].getAsLong().toInt()] = listOf(arguments[2].getAsString()!!)
            arguments[0].getAsLong()
        }
    }
}
```

`update()` returns the affected row's rowid for an insert, `null` for a delete, and either for an
update, depending on whether the rowid itself changed. `onConflict`, from the `VirtualTableUpdateScope`
receiver, reports the `ON CONFLICT` resolution SQLite expects, `IGNORE`/`REPLACE`/`ABORT`/... the
implementation is responsible for actually honoring it.

`Regular`, the third `VirtualTableModule` kind alongside `Eponymous` and `EponymousOnly` used
above, is for a table only ever created through `CREATE VIRTUAL TABLE`, with `create()` (run once,
the first time the table is created) and `connect()` (run every time an existing table is
reconnected to, a fresh connection opened against an already-`ATTACH`-ed database, for instance)
implemented separately, one virtual table type per module, the common case. `Eponymous` uses the
exact same callback for both, usable both as `CREATE VIRTUAL TABLE ... USING name(...)` and
directly as `SELECT * FROM name(...)`. `EponymousOnly`, used above, only implements `connect()`,
so the table is only ever usable directly, by name, never through `CREATE VIRTUAL TABLE`, letting
the whole feature be registered ahead of time without a matching `CREATE VIRTUAL TABLE` statement
anywhere.

`createModule(...)` returns the same `Registration` `createFunction(...)` returns, see
[Scalar functions](#scalar-functions). Closing it unregisters the module, registering another one
under the same name replaces it and makes closing the replaced handle a no-op, and closing a handle
twice is a no-op either way.

## Virtual file systems

`sqlite.virtualFileSystems` finds and manages the VFS SQLite itself already knows about, by name,
plus `default`, the platform's own VFS, the one a connection opened without an explicit `vfs`
argument ends up using:

```kotlin
val defaultVfs = sqlite.virtualFileSystems.default
println(defaultVfs?.zName)

val customVfs = sqlite.virtualFileSystems.find("unix-excl")
```

`register(vfs, makeDefault)`/`unregister(vfs)` add or remove one from SQLite's own registry, so a
connection can later be opened with `vfs = "the-registered-name"`. Writing a brand-new VFS from
Kotlin isn't supported yet, so the `VirtualFileSystemBase` passed to `register()` normally comes
from somewhere else already, wrapping an existing VFS through
[the encryption module](#encryption) is the one example currently available. That one is already
registered as a side effect of creating it, and `close()` unregisters it in turn, so `register()`/
`unregister()` don't come into play at all here, they matter for a `VirtualFileSystemBase` obtained
some other way:

```kotlin
val wrapped = sqlite.ciphers.virtualFileSystems.create(defaultVfs!!, makeDefault = false)
// vfs = wrapped.zName can now be passed to sqlite.open(...)
wrapped.close() // unregisters it too
```

See the root [README's VFS notes](../README.md#project-state) for the current state of, and plans
for, writing a VFS from Kotlin.

[`FileName`](src/commonMain/kotlin/ksqlite/kapi/vfs/FileName.kt), returned by
`DatabaseConnection.getFileName()`, decodes a URI-style filename's query parameters, the ones a
custom VFS or SQLite pragma might rely on, `file:app.db?mode=rwc&cache=shared` for instance:

```kotlin
val fileName = db.getFileName()
val cacheMode = fileName?.getValue("cache") // "shared"
```

## Encryption

[`CipherManager`](src/commonMain/kotlin/ksqlite/kapi/cipher/CipherManager.kt), reachable through
`sqlite.ciphers`, wraps [SQLite3MultipleCiphers](https://github.com/utelle/SQLite3MultipleCiphers),
bundled with every Ksqlite build, adding transparent encryption to a database file.

### Using a builtin cipher

`db.cipherConfig.setCipher(cipher)` picks which cipher a connection uses, and
`DatabaseConnection.setKey()`/`setReKey()` set or change the passphrase it's derived from:

```kotlin
val db = sqlite.open("secret.db")
db.cipherConfig.setCipher(SqliteMcCodecType.CHACHA20, SqliteMcConfigParamPrefix.None)
db.setKey("correct horse battery staple".encodeToByteArray())

db.execute("CREATE TABLE fruits(name TEXT);") // now written to secret.db encrypted
```

The same key has to be set again every time the database is reopened, SQLite has no memory of it
across connections:

```kotlin
val reopened = sqlite.open("secret.db")
reopened.cipherConfig.setCipher(SqliteMcCodecType.CHACHA20, SqliteMcConfigParamPrefix.None)
reopened.setKey("correct horse battery staple".encodeToByteArray()) // same key as above
reopened.execute("SELECT * FROM fruits;") // works, decrypts on the fly

val wrongKey = sqlite.open("secret.db")
wrongKey.cipherConfig.setCipher(SqliteMcCodecType.CHACHA20, SqliteMcConfigParamPrefix.None)
wrongKey.setKey("a different passphrase".encodeToByteArray())
wrongKey.execute("SELECT * FROM fruits;") // throws SQLiteException, looks like a corrupt database
```

`db.cipherConfig` reads and writes both the active cipher and its parameters (KDF iteration count
among them) per connection:

```kotlin
val parameters = db.cipherConfig.parameters(SqliteMcCodecType.CHACHA20)
println(parameters[SqliteMcCodecType.CHACHA20.KDF_ITER])
parameters[SqliteMcCodecType.CHACHA20.KDF_ITER] = 128_000
```

`sqlite.ciphers.config` reads and writes the same options globally instead, applied to every
connection opened afterward that doesn't override them itself. It also carries
`isHmacCheckEnabled`/`setHmacCheckEnabled` and `isLegacyWalEnabled`/`setLegacyWalEnabled`, the same
shape as `setCipher()` above, transient by default, permanent with the right
`SqliteMcConfigParamPrefix`. `db.cipherData.cipherSalt()`/`cipherSaltRaw()` read back the salt
SQLite3MultipleCiphers stored in the database header, as hex text or raw bytes respectively, `null`
for a database that isn't encrypted.

`SqliteMcCodecType` lists every builtin cipher, `AES128`, `AES256`, `CHACHA20`, `SQLCIPHER`,
`RC4`, `ASCON128`, and `AEGIS`, each declaring its own set of parameter objects directly on it,
`KDF_ITER` above is one of `CHACHA20`'s. `sqlite.ciphers.count`, `getIndex(cipher)`, and
`getName(index)` walk the full list of registered ciphers, builtin and dynamic alike, by its
1-based SQLite3MultipleCiphers index instead.

SQLite3MultipleCiphers normally wraps the platform's default VFS with a cipher-aware one and makes
it the default automatically as soon as it initializes. Ksqlite turns that off, so a cipher VFS has 
to be created and selected by hand, see [Virtual file systems](#virtual-file-systems). It's made the default VFS
unless told otherwise, `create()`'s `makeDefault` defaults to `true`, so pass `false` explicitly to
only use it for connections that ask for it by name:

```kotlin
val wrapped = sqlite.ciphers.virtualFileSystems.create(
    vfs = sqlite.virtualFileSystems.default!!,
    makeDefault = false
)

val db = sqlite.open("secret.db", vfs = wrapped.zName)
// ... db.cipherConfig.setCipher(...) / db.setKey(...) as above ...
```

Everything above encrypts a whole database file. SQLite3MultipleCiphers also has
[Value Level Encryption](https://utelle.github.io/SQLite3MultipleCiphers/docs/features/feat_vle/)
(VLE), for encrypting individual values, effectively per-column, in a database that's otherwise
left unencrypted. It's exposed as plain SQL functions as per author choice, callable through
`execute()`/`prepare()` like any other SQL:

```kotlin
db.execute("SELECT sqlite3mc_vle_key('correct horse battery staple');") // once per connection

val encrypted = db.prepare("SELECT sqlite3mc_vle_encrypt(?);").use { statement ->
    statement.parameters.bindString(1, "4111 1111 1111 1111")
    statement.step()!!.getByteArray(0) // a BLOB, safe to store in an otherwise plaintext column
}

val decrypted = db.prepare("SELECT sqlite3mc_vle_decrypt(?);").use { statement ->
    statement.parameters.bindByteArray(1, encrypted!!)
    statement.step()!!.getString(0)
}
```

`sqlite3mc_vle_key()` must be called once per connection before the other two. 
`sqlite3mc_vle_encrypt()`/`sqlite3mc_vle_decrypt()` both take an optional second `scope` argument, 
decrypting with a different scope than the one used to encrypt fails with `SQLiteException`. See 
SQLite3MultipleCiphers' own docs, linked above, for the full parameter list (salt, algorithm, KDF 
options).

### Writing a custom cipher

[`DynamicCipher`](src/commonMain/kotlin/ksqlite/kapi/cipher/DynamicCipher.kt) plugs a cipher scheme
implemented in Kotlin into SQLite3MultipleCiphers itself, used exactly the same way as a builtin
one afterward. This is for a genuinely custom encryption scheme, most applications never need one,
reach for [a builtin cipher](#using-a-builtin-cipher) unless there's a specific reason not to.

A `DynamicCipher` implementation has to answer every question SQLite3MultipleCiphers asks about
one connection's encrypted pages:

```kotlin
class RollingXorCipher(override val salt: Buffer, private val keyOffset: Int) : DynamicCipher {
    var keyByte: Byte = 0

    override val isLegacy = false
    override val pageSize = 0    // 0: let SQLite pick the page size
    override val reserved = 0    // extra bytes per page this cipher needs, e.g. for a HMAC

    override fun generateKey(userPassword: ByteArray, rekey: Int, cipherSalt: Buffer?) {
        // derives keyByte from userPassword; a real cipher would use a proper KDF here,
        // this one is for illustration only, not for securing anything real
        var derived = 0
        for (byte in userPassword) derived = derived xor byte.toInt()
        keyByte = (derived + keyOffset).toByte() // keyOffset: just for the example, see below
    }

    private fun xor(data: Buffer) {
        val bytes = data.readBytes()
        for (i in bytes.indices) bytes[i] = (bytes[i].toInt() xor keyByte.toInt()).toByte()
        data.write(bytes, bytes.size)
    }

    override fun encryptPage(page: Int, data: Buffer, reserved: Int) = xor(data)
    override fun decryptPage(page: Int, data: Buffer, reserved: Int, hmacCheck: Boolean) = xor(data)

    override fun close() {
        salt.close()
    }
}
```

> [!WARNING]
> `RollingXorCipher` above is deliberately trivial, to keep the wiring visible. It provides no
> real confidentiality and must not be used to protect anything.

A `Factory` creates one `DynamicCipher` instance per connection, and describes whatever custom
configuration parameters, beyond the ones every cipher already has, the scheme needs:

```kotlin
class RollingXorCipher(override val salt: Buffer, private val keyOffset: Int) : DynamicCipher {
    //...
    companion object Factory : DynamicCipher.Factory<RollingXorCipher> {
        override val saltSize = 16L // bytes of salt that should be allocated and persisted

        override fun DynamicCipherParameterRegistry.registerParameters() {
            // RollingXorCipher doesn't actually need a configurable parameter, this one exists 
            // purely to show the wiring: how a cipher declares a custom, per-connection Int option 
            // beyond the ones every cipher already has (isLegacy, pageSize, reserved, above).
            register {
                m_name = "key_offset"
                m_value = 0
                m_default = 0
                m_minValue = 0
                m_maxValue = 255
            }
        }

        override fun DynamicCipherCreateScope.create(connection: DatabaseConnection): RollingXorCipher {
            return RollingXorCipher(Buffer.allocate(saltSize), keyOffset = getParameter("key_offset"))
        }
    
        override fun clone(source: RollingXorCipher, target: RollingXorCipher) {
            target.keyByte = source.keyByte
        }
    }
}
```

`create()` runs whenever SQLite3MultipleCiphers needs a new cipher instance for a connection using
this cipher. `getParameter(name)`, from the `DynamicCipherCreateScope` receiver, reads back whatever
a registered parameter was configured to, `key_offset`'s `m_default` above unless a connection
overrode it. `clone()` runs separately, when SQLite needs a second, independent cipher instance
carrying the exact same state as an existing one instead of a fresh one, backing up a database
while it's open, for instance.

Registering the factory, under a name, makes it selectable the same way a builtin cipher is,
`db.cipherConfig.setCipher(name)` instead of `setCipher(SqliteMcCodecType.xxx)`. Its custom
parameters are readable and writable the same way too, by name instead of a constant like 
`SqliteMcCodecType.CHACHA20.KDF_ITER`:

```kotlin
sqlite.ciphers.register("rolling-xor", RollingXorCipher)

val db = sqlite.open("secret.db")
db.cipherConfig.setCipher("rolling-xor", SqliteMcConfigParamPrefix.None) {
    set("key_offset", 42) // just for the example
}
db.setKey("correct horse battery staple".encodeToByteArray())
```

A cipher must be registered before any connection that will use it is opened, a connection's own
copy of the cipher configuration is set up when it's opened, not looked up fresh on every
`setCipher()` call. `register()`'s `makeDefault` also defaults to `true`, so the snippet above,
taken on its own, makes `rolling-xor` the default cipher for every connection opened afterward,
not just the one that calls `setCipher("rolling-xor", ...)` explicitly. Pass `false` when
registering more than one dynamic cipher and only some connections should use a given one.

> [!NOTE]
> At most 4 dynamic ciphers can be registered at once, `register()` throws `IllegalStateException`
> past that. SQLite3MultipleCiphers doesn't pass any context back into the callback responsible for
> creating a cipher instance, so this module can only tell registrations apart through a small,
> fixed pool of distinguishable callbacks, hence the cap.

## Configuration

Configuration in this module falls into two groups, depending on when SQLite allows the underlying
`sqlite3_config()` option to change.

[`AnyTimeConfiguration`](src/commonMain/kotlin/ksqlite/kapi/config/AnyTimeConfiguration.kt),
exposed as `sqlite.config`, covers the handful of options that can change after SQLite is already
initialized. `setLogger()` is the main one, receiving every message SQLite itself logs internally,
plus anything sent through `SQLite.log()`:

```kotlin
sqlite.config.setLogger { errorCode, message -> println("[$errorCode] $message") }
```

Everything else, threading mode, lookaside memory, URI handling, and more, is locked in the moment
`sqlite3_initialize()` runs, and can only be set before that, through the `configure` lambda passed
to `SQLite.initialize()`, which runs in a
[`ConfigurationScope`](src/commonMain/kotlin/ksqlite/kapi/config/ConfigurationScope.kt) that
extends `AnyTimeConfiguration` with those extra, init-time-only options:

```kotlin
val sqlite = SQLite.initialize {
    setSingleThread()             // or setMultiThread() / setSerialized()
    setMemStatusEnabled(true)     // collect memory allocation statistics
    setLookasideConfig(sz = 128, cnt = 500)
    setMmapSize(sz = 0, mx = 256L * 1024 * 1024)
}
```

`setSqlLogger()` is the init-time-only counterpart to `setLogger()`, receiving one
`SqliteSqlLogEvent` per SQL-level event instead of a raw log message: `DatabaseOpened(dbFileName)`,
`StatementExecuted(statement)`, and `DatabaseClosed`, useful for a lightweight, process-wide audit
log of everything SQLite does across every connection:

```kotlin
val sqlite = SQLite.initialize {
    setSqlLogger { connection, event -> println(event) }
}
```

Every other `ConfigurationScope` option is a one-line toggle or setter, `setUriEnabled(enabled)`,
`setCoveringIndexScanEnabled(enabled)`, `setSmallMallocEnabled(enabled)`,
`setStatementJournalSpillThreshold(nByte)`, `setPackedMemoryArraySize(szPma)`,
`setInMemoryDatabaseMaxSize(maxSize)`, and `isRowidInViewActivated`, each documented on
`ConfigurationScope` itself with what it does and why it might matter.
