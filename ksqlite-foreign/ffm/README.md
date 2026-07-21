# Module Ksqlite Foreign FFM

Backs `ksqlite-capi`'s `jvmMain` source set. Desktop JVM, targeting the Java FFM (Panama) API
introduced as a stable feature in JDK 22.

The Java bindings (method handles, layouts, and so on) are generated from `ksqlite.h` by jextract,
wired through [Komple](https://github.com/manriif/komple). Komple also compiles a native shared
library for every supported desktop platform, but only hands it back as a build output. This
module bundles it as a JAR resource instead. A small generated Kotlin file maps the running OS and
architecture to the matching resource path.

`KsqliteFfm.kt` is the only hand-written file. It uses that mapping to extract the matching native
library from resources and load it with `System.load`. Once loaded, all FFM downcalls go through
the jextract-generated bindings.

Compilation here targets JDK 22, separate from the rest of the project which targets JDK 17, since
JDK 22 is the first release where FFM is stable.
