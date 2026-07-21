# Ksqlite Foreign

Internal, per-target bindings to the `ksqlite` C library. None of it is published on its own. It
exists to give `ksqlite-capi` a uniform expect/actual surface, whatever foreign-function facility
a given Kotlin target relies on.

Each submodule backs exactly one `ksqlite-capi` source set:

| Module                 | `ksqlite-capi` source set | Mechanism                      |
|------------------------|---------------------------|--------------------------------|
| [`cinterop`](cinterop) | `nativeMain`              | Kotlin/Native cinterop         |
| [`ffm`](ffm)           | `jvmMain`                 | Java FFM (Panama)              |
| [`jni`](jni)           | `androidMain`             | JNI                            |
| [`wasm`](wasm)         | `webMain`                 | Kotlin `external` declarations |

FFM was picked for desktop JVM over JNI mostly for being the modern option. Bundling or requiring
a recent JDK is no longer a hard sell for a desktop app, and FFM is generally reported to be
faster than JNI (debatable). Android runs on a JVM too, but doesn't ship the FFM module. A backport,
[PanamaPort](https://github.com/vova7878/PanamaPort), does exist, but only from API 26 onward.
Plenty of consumers still target an older API, AndroidX itself down to 23. That's why `jni` falls
back to classic JNI, hand-written C++ shim included, instead of relying on that backport.

Compiling and generating this code relies on [Komple](https://github.com/manriif/komple), the
Gradle plugin extracted from this project to manage native toolchains and C project compilation.
Each submodule's own README covers what part of its code is generated versus hand-written.
