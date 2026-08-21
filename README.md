[sqlite_docs]: https://sqlite.org/docs.html
[ksqlite_docs]: https://manriif.github.io/ksqlite

# Kotlin SQLite

![Stability](https://img.shields.io/badge/stability-experimental-orange.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-dokka-green)][ksqlite_docs]
[![Maven Central](https://img.shields.io/maven-central/v/io.github.manriif.ksqlite/ksqlite-capi?label=Maven%20Central&logo=apache-maven&color=teal)](https://central.sonatype.com/artifact/io.github.manriif.ksqlite/ksqlite-capi)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20--RC-purple.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![SQLite](https://img.shields.io/badge/SQLite-3.53.4-white.svg?logo=sqlite&logoColor=white)](https://sqlite.org)
[![SQLite Multiple Ciphers](https://img.shields.io/badge/SQLite%20Multiple%20Ciphers-2.5.0-red.svg)](https://utelle.github.io/SQLite3MultipleCiphers/)

![platform-jvm](https://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat)
![platform-android](https://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![platform-android--native](https://img.shields.io/badge/platform-android%20native-6EDB8D.svg?style=flat)
![platform-js](https://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat)
![platform-wasmjs](https://img.shields.io/badge/platform-wasm--js-624FE8.svg?style=flat)
![platform-linux](https://img.shields.io/badge/platform-linux-2D3F6C.svg?style=flat)
![platform-windows](https://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat)
![platform-macos](https://img.shields.io/badge/platform-macos-111111.svg?style=flat)
![platform-ios](https://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![platform-watchos](https://img.shields.io/badge/platform-watchos-C0C0C0.svg?style=flat)
![platform-tvos](https://img.shields.io/badge/platform-tvos-808080.svg?style=flat)

Kotlin Multiplatform bindings exposing a near-complete SQLite API, backed by the same SQLite
build across every supported target.

## Table of contents

- [Why Ksqlite](#why-ksqlite)
- [Supported platforms](#supported-platforms)
- [Capabilities](#capabilities)
- [Limitations](#limitations)
- [Project state](#project-state)
- [Choosing an implementation](#choosing-an-implementation)
- [Requirements](#requirements)
- [Getting started](#getting-started)
- [Modules](#modules)
- [Contributing](#contributing)
- [Documentation](#documentation)
- [License](#license)

## Why Ksqlite

This project exists because another, upcoming project of mine needed SQLite features that no
existing "driver-oriented" wrapper covered. As a power user, I also like "owning" my apps. Cutting 
a feature, or piling up workarounds until the app itself starts feeling like one big workaround, 
wasn't something I was willing to accept.

A few recurring pain points from existing solutions pushed me toward writing my own:

- Missing support for a legitimate Kotlin target
- Having to write your own expect/actual code just to configure/load the native library
- Every function marked `suspend` because of one problematic target
- Solutions replicating Android's behavior everywhere, introducing inconsistencies you have to
  work around yourself, even though 90% of your targets aren't Android
- Unaligned compile options and SQLite versions across platforms, leaving you unsure whether a
  given feature is actually available

Ksqlite avoids all of that. The contract is simple: a nearly one-to-one mapping of SQLite's C
API, so the official [SQLite documentation][sqlite_docs] stays your primary reference. Delivering 
that consistently across every target means this project owns the whole pipeline, from compiling 
SQLite itself to shipping the right resources to the end application.

Covering this many SQLite APIs was never the plan. At some point I lost control, purely for the
pleasure of problem-solving, and kept implementing more than I actually needed. At this point
there's little reason left for another SQLite library to show up, unless it can meaningfully cut
the interop cost in hot loops, something most apps never get close to needing.

## Supported platforms

Every Kotlin target is supported, including simulators, except `wasmWasi`:

| Kotlin target   | CPU architecture(s)             | Generated artifact(s)     | CI-tested         |
|-----------------|---------------------------------|---------------------------|-------------------|
| JVM (Linux)     | aarch64, x86_64                 | libksqlite.so             | x86_64            |
| JVM (macOS)     | aarch64, x86_64                 | libksqlite.dylib          | aarch64           |
| JVM (Windows)   | aarch64, x86_64                 | ksqlite.dll               | x86_64            |
| Android (JVM)   | armv7a, aarch64, i686, x86_64   | -                         | x86_64            |
| Android Native  | armv7a, aarch64, i686, x86_64   | libksqlite.a              | -                 |
| Linux           | aarch64, x86_64                 | libksqlite.a              | x86_64            |
| Windows (mingw) | x86_64                          | libksqlite.a              | x86_64            |
| macOS           | arm64, x86_64                   | libksqlite.a              | arm64             |
| iOS             | arm64, x86_64                   | libksqlite.a              | arm64 (simulator) |
| tvOS            | arm64, x86_64                   | libksqlite.a              | arm64 (simulator) |
| watchOS         | armv7k, arm64_32, arm64, x86_64 | libksqlite.a              | arm64 (simulator) |
| JS              | -                               | ksqlite.mjs, ksqlite.wasm | yes               |
| WasmJs          | -                               | ksqlite.mjs, ksqlite.wasm | yes               |

- On JVM, only major operating systems are currently supported, though nothing prevents adding a
  non-major one. Artifacts are embedded in the JAR and can be found at
  `<jar>/native/<linux|macos|windows>_<arch>/<artifact>`.
- On JS and WasmJs, only the browser sub-target is supported, no Node.js. `Long` also compiles down
  to native `BigInt` on JS specifically, see [The JS case](#the-js-case).
- Although SQLite seems to
  [support WASI builds](https://sqlite.org/wasm/doc/trunk/building.md#wasi-sdk), there is currently
  no support for the Kotlin `wasmWasi` target. Support may be added in a future release.

> [!NOTE]
> Every target embeds a compiled library. Expect the final application to grow by a few megabytes, 
> because of it. The library sizes range from 1.5MB to 2.6MB. The Wasm resources are less than 4MB.

> [!IMPORTANT]
> `macosX64`, `tvosX64`, and `watchosX64` were deprecated by Kotlin itself as of 2.3.20. Ksqlite
> still builds and ships them for now.

## Capabilities

- Same SQLite version everywhere, kept at the latest, or near-latest, release
- Some compile options guaranteed, so a given feature's availability is consistent across targets,
  see [Sqlite.kt](compile-logic/src/main/kotlin/Sqlite.kt) for the (non-exhaustive) list of common 
  compile options
- Every build includes [SQLite3MultipleCiphers](https://github.com/utelle/SQLite3MultipleCiphers),
  full encryption support across every target

### SQLite feature coverage

- ~220+ supported SQLite C functions
- Generous configuration support: `sqlite3_config()`, `sqlite3_db_config()`, `sqlite3_vtab_config()`
- Every global and connection-level hook
- Application-defined functions: scalar, aggregate, window
- Backup API
- (De)serialization API
- Incremental blob I/O API
- Snapshot API
- Write-ahead log API
- Virtual tables: regular, eponymous, eponymous-only

### SQLite3 Multiple Ciphers feature coverage

- Complete support of the public C functions
- Full encryption: `sqlite3_key()`/`sqlite3_key_v2()`, `sqlite3_rekey()`/`sqlite3_rekey_v2()`
- Per-value (column) encryption via
  [Value Level Encryption](https://utelle.github.io/SQLite3MultipleCiphers/docs/features/feat_vle/)
- All builtin ciphers: wxSQLite3 (AES 128/256), sqleet (ChaCha20-Poly1305), SQLCipher (AES 256), 
  System.Data.SQLite (RC4), Ascon (Ascon-128), AEGIS
- Custom cipher written in Kotlin via 
  [Dynamic cipher](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/),
  up to 4 concurrently registered
- Per-connection and per-cipher configuration: `sqlite3mc_config()`/`sqlite3mc_config_cipher()`
- Cipher registry and per-connection encryption state introspection:
  `sqlite3mc_cipher_count()`/`sqlite3mc_cipher_index()`/`sqlite3mc_cipher_name()`,
  `sqlite3mc_codec_data()`
- VFS operations: `sqlite3mc_vfs_create()`/`sqlite3mc_vfs_destroy()`/`sqlite3mc_vfs_shutdown()`

> [!IMPORTANT]
> Unlike SQLite3 Multiple Ciphers' own default, its cipher-aware VFS isn't automatically enabled
> here. A cipher VFS, if something needs one, has to be created and selected first, see each 
> implementation's own Encryption section.

### What Ksqlite adds

- Automatic native memory management, with a few explicit exceptions, see
  [choosing a module](#ksqlite-capi)
- Enumerations and sealed hierarchies instead of raw integers, wherever it's meaningful
- SQLite's own semantics preserved almost everywhere
- Automatic native library loading. The first thing you still have to call yourself is
  `sqlite3_initialize()`
- A small `ksqlite.h`/`ksqlite.c` shim on top of SQLite itself: interop-friendly versions of a
  few C functions, and a couple of extra declarations. Extra functions purely to save native
  call round-trips in hot loops are a possibility.

## Limitations

Unless listed below, there's no reason a given SQLite API or feature can't be supported: if it's
missing from the source, it simply hasn't been implemented yet.

### UTF-8 only

None of SQLite's UTF-16 routines are exposed. This isn't really a limitation, more a choice:
adding UTF-16 support on every target is extra work, though some platforms would genuinely benefit 
from it. See [Project state](#project-state) for the current per-target picture and notes on picking it up.

### Virtual tables

The `xShadowName` virtual table hook isn't supported, or at least needs a workaround, due to the
lack of context SQLite provides when invoking it.

## Project state

This project is in active development, and still experimental. Breaking changes can land at any
time, though not many are expected at this point.

**Done:**

- Mastering the SQLite build across every target
- The one-to-one mapping in `ksqlite-capi`
- The object-oriented API in `ksqlite-kapi`
- Encryption via the bundled
  [SQLite3MultipleCiphers API](https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/),
  across every target

**Planned:**

- Proxy API, third implementation sitting next to `ksqlite-capi` and `ksqlite-kapi`, a requirement 
  for using Ksqlite in a WebWorker or even remotely
- [WASM OPFS](https://sqlite.org/wasm/doc/trunk/persistence.md) support, plus WAL mode
- Wrapper functions in `ksqlite.h`/`ksqlite.c`, to cut native call round-trips during large queries

**Not planned:**

- Deprecated SQLite APIs
- Dynamic string APIs (`kotlin.String` and string templates already cover this)
- Mutex APIs
- Windows-specific APIs
- UTF-16 support
- VFS support

<details>
<summary>Notes for whoever wants to pick up UTF-16 support</summary>

Readiness varies by target. Native and JVM already have good support for it. Android JVM ended
up accidentally ready for it during development. JS and WasmJs need a non-negligible amount of
work.

UTF-16 functions need to be:

- Uncommented in
  [`SqliteTextEncoding`](ksqlite-types/core/src/commonMain/kotlin/ksqlite/types/SqliteTextEncoding.kt)
- Un-omitted in [Sqlite.kt](compile-logic/src/main/kotlin/Sqlite.kt)
- Un-omitted in the [WASM build](ksqlite/ext/wasm/GNUmakefile), exported in
  [Wasm.kt](compile-logic/src/main/kotlin/modules/Wasm.kt), and declared in
  [Sqlite3WasmExports.kt](ksqlite-foreign/wasm/src/webMain/kotlin/ksqlite/foreign/Sqlite3WasmExports.kt)
- Declared in [KsqliteJni.kt](ksqlite-foreign/jni/src/main/kotlin/ksqlite/foreign/KsqliteJni.kt)
  and implemented in [ksqlite-jni.cpp](ksqlite-foreign/jni/src/main/cpp/ksqlite-jni.cpp) on
  Android, watch out for memory leaks
- Expect/actualized in `ksqlite-capi`

For JS and WasmJs specifically, the
[Unicode utilities borrowed from AOSP](ksqlite-foreign/jni/src/main/cpp/utils/Unicode.cpp) could
help, once moved to [ksqlite](ksqlite) and stripped of their C++ and Android logging
dependencies. What's left after that is finding a native JS function that converts a UTF-16
buffer straight to a `JsString`, then to a Kotlin `String`.

`ksqlite-kapi` would then need to decide whether UTF-16 becomes its default encoding.

</details>

<details>
<summary>Notes for whoever wants to pick up VFS support</summary>

An implementation already exists in `ksqlite-capi`, letting Kotlin **invoke hooks of a
C-written VFS**:

- Struct classes for `sqlite3_io_methods` and `sqlite3_vfs` already exist
- The remaining SQLite hooks for those structs still need a `fun interface` in `commonMain`
- Their implementation on `androidMain`, `jvmMain`, `nativeMain`, and `webMain` needs finishing

The more interesting direction is the reverse: letting SQLite **invoke hooks of a Kotlin-written
VFS**, the way virtual tables already do. Part of the groundwork is already in
[ksqlite-foreign](ksqlite-foreign), which exposes the VFS-related structs (`sqlite3_io_methods`,
`sqlite3_file`, `sqlite3_vfs`). Only a few Android-side declarations are still missing for both
directions.

`ksqlite-kapi` would eventually need its own VFS API on top of this.

</details>

None of this is set in stone, but my attention has been drifting toward the bigger project this
one was born from, and the pressure to find a job is real. I've heard time can be bought,
though 😈

## Choosing an implementation

Multiple implementations, covering the same set of features, are available.

Every implementation goes through a native call, which costs a few nanoseconds each time. That's
irrelevant for a handful of rows, but caching a value read out of an interop call starts to
matter once a hot loop iterates over tens of thousands of rows or more. Wrapper functions are 
planned to further reduce these costs, particularly when pagination is not possible or desired.

Some [enumerations and sealed hierarchies](ksqlite-types/core/README.md) are shared across every
implementation.

> [!TIP]
> Enabling Kotlin's context-sensitive resolution makes working with Ksqlite's enumerations and
> sealed hierarchies noticeably nicer, short names instead of fully qualified ones.

### [`ksqlite-capi`](ksqlite-capi/README.md)

The main module of this project.

- Automatic memory management, with a few exceptions described 
  [here](ksqlite-capi/README.md#memory-management)
- Automatic loading of the Ksqlite library
- The one-to-one mapping of the SQLite C API
- Mostly top-level declarations
- Never throws. If it does, one of the following is true:
    - it's a bug, please open an issue
    - a hard contract was violated, check the documentation
    - the VM ran out of memory (platform-dependent), watch for your platform's low-memory signal
- Every other module, current or future, depends on it directly or indirectly

`ksqlite-capi` is the lightest module and the closest to raw SQLite, at the cost of a
Kotlin-unfriendly API. Pick it for minimal dependencies, or as a base to build your own
object-oriented API on top of.

> [!WARNING]
> `ksqlite-capi` needs to be [initialized](ksqlite-capi/README.md#initialization).

> [!TIP]
> For Android and JVM, Ksqlite can be loaded eagerly by initializing SQLite at the desired time 
> (for example, at application startup).

### [`ksqlite-kapi`](ksqlite-kapi/README.md)

One of infinitely many possible object-oriented APIs for SQLite.

- Wraps `ksqlite-capi` without exposing it, don't depend on both at once
- A Kotlin-friendly API
- `kotlin.Boolean` is back
- Default parameter values
- Extended memory management, able to release some of the resources `ksqlite-capi` can't, see
  [here](ksqlite-capi/README.md#global-callbacks)
- Every lifecycle-aware resource implements `AutoCloseable`, the Kotlin standard library
  handles the rest
- Stronger protection against `SQLITE_MISUSE`, only exposing the API available in the current
  context
- Throws `SQLiteException` wherever SQLite can fail, instead of returning a result code
- Throws `IllegalStateException` when accessing a closed resource, or attempting an operation
  with potentially undesirable side effects

`ksqlite-kapi` is generally simpler and safer than `ksqlite-capi`, though `try`/`catch` may still be
everywhere in your code, like with any SQLite library. It should be your default choice, unless you
target Wasm and need OPFS. That still needs the third module.

## Requirements

- Kotlin 2.4.0 is the baseline
- Android `minSdk` 21 or above
- ESM and BigInt-backed `Long` on Kotlin/JS, see [The JS case](#the-js-case)
- JDK 17+ or 22+ if desktop JVM is targeted since Ksqlite makes use of Java FFM
- Gradle:
    - 9.1.0+ if the [Ksqlite Gradle plugin](#wasm-specific) is not needed
    - 9.4.0+ otherwise, as modern lazy API is used by the plugin

### The JS case

SQLite's C API relies on 64-bit integers throughout, row ids included. To keep that precision
consistent with every other target, Ksqlite enables a 64-bit WASM build, so those values cross the
JS↔WASM boundary as native `BigInt`. Kotlin/JS's `Long` doesn't use that representation by default, 
and mixing the two crashes at runtime.

Consuming projects need `useEsModules()`, and may need to enable `BigInt` support too:

```kotlin
// build.gradle.kts
kotlin {
    js {
        useEsModules()

        compilerOptions {
            freeCompilerArgs.add("-Xes-long-as-bigint")
        }
    }
}
```

This compiler flag isn't required for `WasmJs`.

> [!NOTE]
> `-Xes-long-as-bigint` is still an experimental Kotlin compiler flag.

## Getting started

### `ksqlite-capi`

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.manriif.ksqlite:ksqlite-capi:<version>")
        }
    }
}
```

From there, this is close to using SQLite's C API directly. The snippet below uses `check` for
result validation, for simplicity, and assumes Kotlin's context-sensitive resolution is enabled
so bare names like `OK` resolve to `SqliteResultCode.OK`:

```kotlin
val initResult = sqlite3_initialize()

if (initResult != SqliteResultCode.OK) {
    error("SQLite initialization failed")
}

val outDb = sqlite3.OutputParam()
val openResult = sqlite3_open(":memory:", outDb)

val db = if (openResult.isOk) {
    // outDb.value is guaranteed to be non-null if and only if openResult is SqliteResultCode.OK
    outDb.value!!
} else {
    // sqlite3_open[_v2] may return a failure result code but still a non-null sqlite3 object,
    // left in an 'error state'
    val errMsg = outDb.value?.let(::sqlite3_errmsg) ?: "attach your debugger, problems started"
    error("Open connection failed with result $openResult: $errMsg")
}

check(sqlite3_exec(db, "CREATE TABLE fruits(name TEXT NOT NULL);", null, null, null) == OK)

val outInsert = sqlite3_stmt.OutputParam()
check(sqlite3_prepare_v2(db, "INSERT INTO fruits VALUES (?);", outInsert) == OK)

val insert = checkNotNull(outInsert.value)
check(sqlite3_bind_text(insert, 1, "Kiwi") == OK)
check(sqlite3_step(insert) == DONE)
check(sqlite3_finalize(insert) == OK)

val outSelect = sqlite3_stmt.OutputParam()
check(sqlite3_prepare_v2(db, "SELECT name FROM fruits;", outSelect) == OK)
val select = checkNotNull(outSelect.value)

while (sqlite3_step(select) == ROW) {
    println(sqlite3_column_text(select, 0))
}

check(sqlite3_finalize(select) == OK)
check(sqlite3_close(db) == OK)
check(sqlite3_shutdown() == OK)
```

### `ksqlite-kapi`

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

Most of the calls from below snippet can throw an `SQLiteException` carrying the error code and 
message. They aren't caught for brevity:

```kotlin
val sqlite = SQLite.initialize()
val db = sqlite.open(":memory:")

db.execute("CREATE TABLE fruits(name TEXT NOT NULL);")

db.prepare("INSERT INTO fruits VALUES (?);").use { insert ->
    insert.parameters.bindString(1, "Kiwi")
    insert.step()
}

db.prepare("SELECT name FROM fruits;").use { select ->
    select.forEachRow { row ->
        println(row.getString(0))
    }
}

db.close()
sqlite.close()
```

### WASM specific

When targeting JS or WasmJs, the [`ksqlite-gradle-plugin`](ksqlite-gradle-plugin/README.md) must
also be applied, in the final application module, intermediate modules don't need it:

```kotlin
// webApp/build.gradle.kts
plugins {
    id("io.github.manriif.ksqlite") version "<version>"
}
```

See its README for how to enable a test runner, or for more detail on what the plugin does.

## Modules

| Module                                                 | Description                                                                           |
|--------------------------------------------------------|---------------------------------------------------------------------------------------|
| [`ksqlite-capi`](ksqlite-capi)                         | Kotlin Multiplatform binding to the SQLite C API.                                     |
| [`ksqlite-kapi`](ksqlite-kapi)                         | Object-oriented Kotlin API built on top of `ksqlite-capi`.                            |
| [`ksqlite-gradle-plugin`](ksqlite-gradle-plugin)       | Wires Ksqlite's WASM resources into consuming Kotlin Multiplatform projects.          |
| [`ksqlite-wasm-resources`](ksqlite-wasm-resources)     | Compiled ksqlite WASM artifacts for Kotlin/JS and Kotlin/Wasm.                        |
| [`ksqlite-types/core`](ksqlite-types/core)             | Public enumerations and sealed types modeling SQLite's finite value spaces.           |
| [`ksqlite-types/internal`](ksqlite-types/internal)     | Converts raw SQLite integers into their typed counterparts from `ksqlite-types/core`. |
| [`ksqlite-foreign/cinterop`](ksqlite-foreign/cinterop) | Kotlin/Native cinterop bindings for ksqlite.                                          |
| [`ksqlite-foreign/ffm`](ksqlite-foreign/ffm)           | Java FFM bindings for ksqlite on desktop JVM.                                         |
| [`ksqlite-foreign/jni`](ksqlite-foreign/jni)           | JNI bindings for ksqlite on Android.                                                  |
| [`ksqlite-foreign/wasm`](ksqlite-foreign/wasm)         | Kotlin external bindings for the ksqlite WASM build.                                  |
| [`ksqlite-structs`](ksqlite-structs)                   | Struct memory-layout machinery for `ksqlite-foreign/jni` and `ksqlite-foreign/wasm`.  |
| [`ksqlite-internal/runtime`](ksqlite-internal/runtime) | Internal code shared between whichever modules need it.                               |
| [`ksqlite-internal/test`](ksqlite-internal/test)       | Shared test utilities for whichever modules need it.                                  |

## Contributing

Bug reports and pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for how to
build this project locally, the hardware and IDE it currently expects, and how to run its tests.

UTF-16 and VFS support are two ready-made opportunities if you're looking for a way in, see
[Project state](#project-state) for notes on both.

## Documentation

Documentation for the `ksqlite-capi` module is available [here][sqlite_docs].
For the other modules, [Dokka](https://kotlinlang.org/docs/dokka-introduction.html) generated one is
[there][ksqlite_docs].

## License

Kotlin SQLite is licensed under the [Apache 2.0 License](LICENSE).
