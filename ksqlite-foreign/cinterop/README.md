# Module Ksqlite Foreign CInterop

Backs `ksqlite-capi`'s `nativeMain` source set. Kotlin/Native targets (Android Native, Apple,
Linux, mingW) that talk to `ksqlite` through the standard Kotlin/Native cinterop mechanism.

The static library and its `.def` file are generated for each native target by
[Komple](https://github.com/manriif/komple), from the same `ksqlite` C project the rest of Ksqlite
builds against. The resulting klib exposes `ksqlite.h` under the `ksqlite.foreign` package, so
there's no handwritten Kotlin wrapper to keep in sync.

`KsqliteCInterop.kt` is the only handwritten file, and it's a no-op. Kotlin doesn't produce a
`.klib` for a source set with no Kotlin sources of its own, so an empty public function is enough
to force one out.