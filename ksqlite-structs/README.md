# Module Ksqlite Foreign WASM

Backs `ksqlite-capi`'s `webMain` source set. Browser only, no Node.

The upstream here is the official [SQLite WASM](https://sqlite.org/wasm/doc/trunk/api-wasm.md)
build. Everything in this module is hand-written, `external` Kotlin declarations mirroring that
build's JS API. Where the official build exposes a single, dynamically
assembled JavaScript object (`sqlite3`, with its `capi` and `wasm` sub-objects), this module
splits the same surface across several files instead (`Sqlite3.kt`, `Sqlite3Capi.kt`,
`Sqlite3Wasm.kt`, plus `structs/`, `wasm/` and `js/` for the supporting pieces).

See [`ksqlite-wasm-resources`](../ksqlite-wasm-resources/README.md) for how the WASM build
itself is produced. `KsqliteWeb.kt` is the entry point here, importing the `sqlite3` instance from
the bootstrap module bundled alongside its artifacts.
