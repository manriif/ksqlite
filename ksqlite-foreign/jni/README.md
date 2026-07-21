# Module Ksqlite Foreign JNI

Backs `ksqlite-capi`'s `androidMain` source set. Android, via classic JNI.

Almost everything here is hand-written. The JNI shim (`ksqlite-jni.cpp`) implements every native
method, `KsqliteJni.kt` declares the matching `external fun`s, and `callbacks/` holds the
interfaces `ksqlite-capi` implements to receive upcalls from native code.

Compiling it is the Android Gradle Plugin's own CMake/NDK build, not
[Komple](https://github.com/manriif/komple). Komple only supplies the shared C sources, include
paths, compile definitions, and library name, passed in as CMake arguments so this build stays
aligned with what Komple compiles for every other target.

The `structs/` package wraps SQLite's C structs (`sqlite3_vfs`, `sqlite3_module`, `sqlite3_vtab`,
and so on) for custom VFS and virtual table implementations. Each wrapper reads and writes its
fields over a `ByteBuffer` pointing at the native struct, at offsets it doesn't hardcode. The C++
side computes them with `offsetof`, so they hold for the real layout on the target ABI. The Kotlin
side only tracks the member declaration order, and matches it against that layout by index.

The only generated piece is a single native library name constant, kept in sync with Komple's
shared C project.

Some utility files, namely the UTF-16 helpers in `utils/`, are adapted from AOSP rather than
written from scratch. UTF-16 support itself isn't exposed yet, see the
[root README](../../README.md#project-state) for why and for notes on picking it up.
