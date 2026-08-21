# Module Ksqlite Types

Public enumerations and sealed types for the finite value spaces of the SQLite C API. Result
codes, flags, action codes, SQLite3MultipleCiphers' cipher configuration, and a handful of struct
shapes for VFS and virtual table implementations.

Wherever a C function or struct field only ever takes one of a fixed set of values, this module
gives it a real Kotlin type instead of a raw `Int`. Passing the wrong value becomes a compile
error, not a misuse SQLite may or may not catch at runtime.

These types are shared between all the different API implementations such as `ksqlite-capi` and
`ksqlite-kapi`. Duplicating them would mean duplicating everything baked into them.

Every snippet below uses `ksqlite-capi`, the API these types were designed against first. They
work the same way in `ksqlite-kapi` or any other implementation.

## Table of contents

- [Constants](#constants)
- [Sealed types](#sealed-types)
    - [Combinable flags](#combinable-flags)
    - [Primary and extended codes](#primary-and-extended-codes)
    - [Narrowing to a known subset](#narrowing-to-a-known-subset)
    - [Known and custom values](#known-and-custom-values)
    - [Outcomes and events](#outcomes-and-events)
- [VFS and virtual table shapes](#vfs-and-virtual-table-shapes)
- [Cipher types](#cipher-types)

## Constants

The plain case is an `enum class` with one value per case, passed or returned as-is. A few of
these are named after a SQLite macro ending in `_FLAG` for reasons that only make sense in C. The
values themselves are still mutually exclusive, never combined.

Used as a parameter, `SqliteCheckpointMode` picks how `sqlite3_wal_checkpoint_v2()` should
checkpoint the WAL:

```kotlin
// ksqlite-capi
sqlite3_wal_checkpoint_v2(db, null, SqliteCheckpointMode.PASSIVE, outNLog, outNCkpt)
```

Used as a return value, `SqliteDbReadonlyResult` replaces the `0`/`1`/`-1` that
`sqlite3_db_readonly()` actually returns:

```kotlin
// ksqlite-capi
when (sqlite3_db_readonly(db, "main")) {
    SqliteDbReadonlyResult.READWRITE -> println("writable")
    SqliteDbReadonlyResult.READONLY -> println("read-only")
    SqliteDbReadonlyResult.UNKNOWN_DATABASE -> println("no such database")
}
```

Other types in this group: `SqliteDataType`, `SqliteBlobOpenFlag`, `SqliteAccessFlag`,
`SqliteAuthorizerStatus`, `SqliteTransactionState`, `SqliteExplainMode`,
`SqliteConflictResolutionMode`, `SqliteRuntimeLimit`, and the `SqliteStatusOption`/
`SqliteDbStatusOption`/`SqliteStatementStatusCounter` family used with SQLite's status and limit
APIs.

## Sealed types

A plain `enum class` stops being enough once values need to combine, a context needs to promise
something stronger than "any of these", or a case needs to carry its own data. This module reaches
for a `sealed class` or `sealed interface` for these reasons, covered one at a time below. Nesting
shows up in some of them and not others. It isn't the point of being sealed on its own.

### Combinable flags

Several C APIs take an OR-ed combination of bit flags. `sqlite3_open_v2()`'s third argument and
`sqlite3_prepare_v3()`'s flags are two examples. Each flag is a case of a `sealed class`.
Combining two of them produces a `Mask`, itself a case of that same sealed class, holding the
OR-ed value:

```kotlin
// ksqlite-capi
sqlite3_open_v2(
    fileName = "test_connection",
    outDb = outDb,
    flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
    vfs = null
)
```

`or`, `and`, `without`, and `contains` are all declared on the sealed base. A mask can be passed
anywhere a single flag can. The hierarchy also narrows what is valid where. `sqlite3_open_v2()`'s
`flags` parameter is typed `SqliteOpenFlag.Db`, so a VFS-only flag like `SqliteOpenFlag.AUTOPROXY`
cannot be passed there at all, not even by mistake. The compiler rejects it before SQLite ever
gets a chance to:

```kotlin
// ksqlite-capi
sqlite3_open_v2(
    fileName = "test_connection",
    outDb = outDb,
    // doesn't compile: AUTOPROXY is a `SqliteOpenFlag.OptionalVfs` flag, `or` on a `Db` flag like
    // READWRITE only accepts another `OptionalDb`
    flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.AUTOPROXY,
    vfs = null
)
```

Combining also has to start from the right case. SQLite's own docs for `sqlite3_open_v2()`
require its `flags` parameter to include one of exactly three combinations: `SQLITE_OPEN_READONLY`,
`SQLITE_OPEN_READWRITE`, or `SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE`. Anything else is
undefined behavior. `SqliteOpenFlag` encodes that by making `READONLY` and `READWRITE` the only
two cases of a `Required` sealed class. The `or` that builds a `Db` mask is declared on `Db`,
which `Required` extends, not on `OptionalDb`. Two optional flags like `URI` and `MEMORY` can't be
OR-ed together on their own. A `Db` mask always starts from `READONLY` or `READWRITE`.

Other types in this group: `SqlitePrepareFlag`, `SqliteDeserializeFlag`, `SqliteSerializeFlag`,
`SqliteTraceEventCode`, and `vtab.SqliteVtabScanFlag`.

### Primary and extended codes

SQLite result codes come in two layers. A "primary" code like `SQLITE_BUSY`, and a family of more
specific "extended" codes refining it, like `SQLITE_BUSY_RECOVERY`, `SQLITE_BUSY_SNAPSHOT`, and
`SQLITE_BUSY_TIMEOUT`. `SqliteResultCode` models that directly. Each primary code is a sealed
class of its own, exposed as a singleton through its `companion object`, with its extended
variants nested inside as `data object`s:

```kotlin
// ksqlite-capi
when (val result = sqlite3_open_v2(fileName, outDb, SqliteOpenFlag.READWRITE, null)) {
    SqliteResultCode.OK -> { /* ... */ }
    SqliteResultCode.BUSY.SNAPSHOT -> { /* ... */ }
    is SqliteResultCode.BUSY -> { /* also matches BUSY.RECOVERY and BUSY.TIMEOUT */ }
    is SqliteResultCode.Failure -> error("Failed to open database: $result")
    else -> Unit
}
```

`SqliteResultCode.Failure` is a shared marker for every code that represents an error. Code that
only cares about success or failure can match on that, instead of on each of the roughly thirty
individual failure codes.

Not every code is one or the other, though. `DONE` and `ROW` are progress signals from
`sqlite3_step()`, not an outcome, and sit outside the success/failure split entirely. That split
is captured by `SqliteResultCode.OkOrFailure`, a marker that `OK` and every `Failure` extend, but
`DONE` and `ROW` don't. Matching on it means "an actual outcome, not a step continuation."

### Narrowing to a known subset

SQLite sometimes guarantees a narrower set of values than a hierarchy's full range, documented as
such in its own API reference. That narrower subset gets its own marker wherever it applies.
`sqlite3_update_hook()` and `sqlite3_preupdate_hook()`, for instance, only ever report an
`INSERT`, `UPDATE`, or `DELETE`, never one of the other action codes. Their callback parameter is
typed `SqliteActionCode.RowChange`, the marker those three cases extend, instead of the full
`SqliteActionCode`:

```kotlin
// ksqlite-capi
sqlite3_update_hook(db, appData) { _, action, dbName, tableName, rowid ->
    when (action) {
        SqliteActionCode.INSERT -> println("inserted into $tableName")
        SqliteActionCode.UPDATE -> println("row $rowid updated in $tableName")
        SqliteActionCode.DELETE -> println("row $rowid deleted from $tableName")
    }
}
```

That subset is fixed by SQLite's own contract for this particular API. The `when` above is
already exhaustive over exactly those three cases.

`sqlite3_set_authorizer()`, on the other hand, can report any action it authorizes. Its callback
parameter stays typed as the full `SqliteActionCode`. A `when` narrowed to the same three cases
isn't exhaustive anymore:

```kotlin
// ksqlite-capi
sqlite3_set_authorizer(db, appData) { _, action, _, _, _, _ ->
    // doesn't compile: `action` is a `SqliteActionCode`, not a `SqliteActionCode.RowChange`, so 
    // this `when` isn't exhaustive, INSERT, SELECT, ATTACH, and about 30 other cases aren't handled
    when (action) {
        SqliteActionCode.CREATE_TABLE -> SqliteAuthorizerStatus.OK
        SqliteActionCode.DROP_TABLE -> SqliteAuthorizerStatus.DENY
        SqliteActionCode.PRAGMA -> SqliteAuthorizerStatus.IGNORE
    }
}
```

The same idea can narrow along more than one axis at once. `SqliteTextEncoding` implements a
separate marker interface for each SQLite API that accepts a text encoding: `BindText` for
`sqlite3_bind_text64()`, `CreateCollation` for `sqlite3_create_collation()`, and so on. `UTF8`
implements all of them since every such API currently supports it. A narrower encoding would only
implement the ones it's actually valid for. The create-function family relies on this too:

```kotlin
// ksqlite-capi
sqlite3_create_function_v2(
    db = db,
    name = "pow2",
    nArg = 2,
    encoding = SqliteTextEncoding.UTF8 or SqliteFunctionFlag.RESULT_SUBTYPE,
    appData = null,
    step = null,
    final = null,
    func = { _, context, values -> /* ... */ },
    destroy = null
)
```

`or` here only accepts an encoding that also implements `SqliteFunctionTextEncoding`. Pairing
`SqliteFunctionFlag.RESULT_SUBTYPE` with an encoding the create-function API doesn't support
wouldn't compile.

`SqliteFunctionFlag`'s own values are bit flags too. `DETERMINISTIC`, `DIRECTONLY`, `INNOCUOUS`,
`RESULT_SUBTYPE`, `SELFORDER1`, and `SUBTYPE` are meant to be OR-ed together, the same "start from
the right case" shape as `SqliteOpenFlag` in [Combinable flags](#combinable-flags). Here the
mandatory starting case is an encoding, not a flag. `or` is declared on
`SqliteFunctionTextEncoding`, not on `SqliteFunctionFlag` itself.
`SqliteFunctionFlag.DETERMINISTIC or SqliteFunctionFlag.DIRECTONLY` doesn't compile. Only
`SqliteTextEncoding.UTF8 or SqliteFunctionFlag.DETERMINISTIC or SqliteFunctionFlag.DIRECTONLY`
does. Every create-function call already requires an encoding, so this isn't a real limitation in
practice. It does mean `SqliteFunctionFlag` isn't the plain, mutually-exclusive enum the
[Constants](#constants) section describes.

### Known and custom values

`SqliteVtabConstraintOperatorCode` mixes a set of named constants (`EQ`, `GT`, `MATCH`, ...) with
operator codes that only exist because a virtual table's `xFindFunction` registered them at
runtime. Those application-defined operators aren't part of any fixed list. Instead of a case per
value, they all fall into one `Custom` case carrying whichever code SQLite assigned it:

```kotlin
// ksqlite-capi
when (val op = indexInfo.getConstraintOp(0)) {
    SqliteVtabConstraintOperatorCode.EQ -> { /* ... */ }
    SqliteVtabConstraintOperatorCode.MATCH -> { /* ... */ }
    is SqliteVtabConstraintOperatorCode.Custom -> { println("xFindFunction op code: ${op.code}") }
    else -> { /* ... */ }
}
```

### Outcomes and events

Sometimes a C return value or a callback argument isn't just "one of N values", it's an actual
outcome or event. Some cases need to carry their own data and others don't, so a sealed type
models that directly instead of reusing a result code. `SqliteCompleteResult`, the result of
`sqlite3_complete()`, is the example here. A plain `Complete` case, a plain `Incomplete` case, and
a `Failure` case that carries the actual `SqliteResultCode.Failure` it wraps.

```kotlin
// ksqlite-capi
when (val result = sqlite3_complete("CREATE TABLE test")) {
    SqliteCompleteResult.Complete -> println("complete")
    SqliteCompleteResult.Incomplete -> println("incomplete")
    is SqliteCompleteResult.Failure -> println(result.result)
}
```

`SqliteSqlLogEvent`, the event delivered to an SQLLOG callback, is built the same way. Its
`DatabaseOpened` and `StatementExecuted` cases each carry their own data, while `DatabaseClosed`
carries none.

`ksqlite-capi` and `ksqlite-kapi` each define a few more types shaped like this for their own
APIs. This module only holds the ones shared between the two.

## VFS and virtual table shapes

[`vfs`](src/commonMain/kotlin/ksqlite/types/vfs) and
[`vtab`](src/commonMain/kotlin/ksqlite/types/vtab) hold a different kind of type, plain
interfaces. Their only purpose is to expose the base API of SQLite's `vtab` and `vfs` related 
structs, shared between `ksqlite-capi` and `ksqlite-kapi`. Each struct's
version field is itself one of the plain constants from the [Constants](#constants) group:
`SqliteVfsVersion`, `SqliteIoMethodsVersion`, and `SqliteModuleVersion`.

These interfaces only cover the part of each struct that's the same regardless of who implements
it, plain data fields such as `SqliteVfs.mxPathname`, `SqliteVfs.pNext`, or `SqliteVtab.errMsg`.
The C structs are mostly made of function pointers, `sqlite3_vfs.xOpen`, `sqlite3_io_methods.xRead`,
`sqlite3_module.xBestIndex`, and so on. Those are implementation-defined. `ksqlite-capi`,
`ksqlite-kapi`, and any implementation added later each decide how to represent and wire up that
part on their own, so this module doesn't attempt to abstract it.

## Cipher types

[SQLite3MultipleCiphers](https://github.com/utelle/SQLite3MultipleCiphers) is bundled with every
build, adding transparent database encryption on top of SQLite. This module models its
configuration surface.

`SqliteMcCipher` identifies which cipher a parameter belongs to, the same
[known and custom values](#known-and-custom-values) shape as `SqliteVtabConstraintOperatorCode`.
`SqliteMcCodecType` is the known side, one `data object` per builtin cipher (`AES128`, `AES256`,
`CHACHA20`, `SQLCIPHER`, `RC4`, `ASCON128`, `AEGIS`), each also a `sealed class` nesting its own
`Param<Value>` cases, since builtin ciphers don't all take the same parameters. `Dynamic` is the
custom side, keyed by whatever name a
[dynamically registered cipher](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/)
was given, its `Parameter` accepting any `Int`, since a dynamic cipher's own parameters aren't
known ahead of time.

`SqliteMcConfigCipherParam<Cipher, Value>` ties a parameter to one specific `Cipher`, the same
[narrowing](#narrowing-to-a-known-subset) pattern as `SqliteActionCode.RowChange`, so a `CHACHA20`
parameter like `KDF_ITER` can't be paired with an `AES128` cipher, even though both are
`SqliteMcCodecType`:

```kotlin
// ksqlite-capi
val kdfIter = sqlite3mc_config_cipher(
    db,
    SqliteMcCodecType.CHACHA20,
    SqliteMcCodecType.CHACHA20.KDF_ITER,
    SqliteMcConfigParamPrefix.None
)

// doesn't compile: KDF_ITER is a SqliteMcConfigCipherParam<CHACHA20, Int>, AES128 isn't CHACHA20
sqlite3mc_config_cipher(
    db,
    SqliteMcCodecType.AES128,
    SqliteMcCodecType.CHACHA20.KDF_ITER,
    SqliteMcConfigParamPrefix.None
)
```

`SqliteMcConfigParamPrefix` controls whether a config function reads or writes the transient or
permanent value (`None`/`Default`), or the valid range instead (`Min`/`Max`). Only `None` and
`Default` extend `ReadWrite`, so `sqlite3mc_config()`/`sqlite3mc_config_cipher()`'s write overload
only accepts one of those two, `Min`/`Max` are read-only by construction, not just by convention.

`SqliteMcCipherDescriptor` and `SqliteMcCipherParams` are the same kind of type as
[VFS and virtual table shapes](#vfs-and-virtual-table-shapes) above, plain interfaces over the
`CipherDescriptor`/`CipherParams` C structs a dynamic cipher registers itself with. The
function-pointer part, the actual cipher callbacks, is left to each API implementation.

`SqliteMcLegacyPageSize`, one of the [Constants](#constants) group, lists the page sizes several
ciphers' `LEGACY_PAGE_SIZE` parameter accepts.
