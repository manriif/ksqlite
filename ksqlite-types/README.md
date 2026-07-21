# Ksqlite Types

The type system shared by `ksqlite-capi` and `ksqlite-kapi`, split across two modules.

## [`core`](core)

Public enumerations and sealed types modeling the finite value spaces of the SQLite C API: result
codes, combinable flags, action codes, and a handful of struct shapes for VFS and virtual table
implementations.

## [`internal`](internal)

The machinery converting a raw integer coming back from SQLite, through `ksqlite-foreign`, into one
of `core`'s types. It isn't meant for public use, regardless of how it ends up wired into the 
dependency graph as more modules potentially join it.