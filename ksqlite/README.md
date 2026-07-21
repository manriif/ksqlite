# Ksqlite

The `ksqlite.h`/`ksqlite.c` shim compiled alongside upstream SQLite into the `ksqlite` C
library every [foreign](../ksqlite-foreign) implementation binds to. It's intended for patching
and extending SQLite's own C API.

`ext/wasm` holds the small patches applied on top of SQLite's own WASM build files, used by
[`ksqlite-wasm-resources`](../ksqlite-wasm-resources) when compiling for the web.
