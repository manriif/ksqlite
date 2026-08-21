# Module Ksqlite Foreign JNI

Backs `ksqlite-capi`'s `androidMain` source set. Android, via classic JNI.

Almost everything here is hand-written. The JNI shim (`ksqlite-jni.cpp`) implements every native
method, `KsqliteJni.kt` declares the matching `external fun`s, and `callbacks/` holds the
interfaces `ksqlite-capi` implements to receive upcalls from native code.

Compiling it is the Android Gradle Plugin's own CMake/NDK build, not
[Komple](https://github.com/manriif/komple). Komple only supplies the shared C sources, include
paths, compile definitions, and library name, passed in as CMake arguments so this build stays
aligned with what Komple compiles for every other target.

The struct wrappers themselves, `sqlite3_vfs`, `sqlite3_module`, `sqlite3_vtab`, and so on for
custom VFS and virtual table implementations, live in [`ksqlite-structs`](../../ksqlite-structs),
shared with `ksqlite-foreign/wasm`. The `structs/` package here only plugs that module's
`Struct.Adapter`/`Struct.Memory` into a direct `ByteBuffer`, and its `StructLayoutProvider` into
`structLayout()`, a JNI call into `ksqlite_struct_layout_allocate()`, caching the result per struct
type since the underlying layout never changes at runtime.

The only generated piece is a single native library name constant, kept in sync with Komple's
shared C project.

Some utility files, namely the UTF-16 helpers in `utils/`, are adapted from AOSP rather than
written from scratch. UTF-16 support itself isn't exposed yet, see the
[root README](../../README.md#project-state) for why and for notes on picking it up.
