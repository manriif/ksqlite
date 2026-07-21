# Module Ksqlite Gradle Plugin

Gradle plugin that wires Ksqlite's WASM resources into a consuming Kotlin Multiplatform project,
without any manual setup. It only acts on projects that also apply the Kotlin Multiplatform
plugin.

## What it does

- For every JS or Wasm target, resolves the matching per-target "resources" Maven variant
  published by `ksqlite-wasm-resources`, extracts it, and wires the result into that target's
  main compilation as generated resources. `ksqlite.wasm`, its `ksqlite.mjs` glue, and the
  bootstrap module all land in the final web output this way
- Reads the version to resolve from a `version.txt` generated at build time from this module's
  own version, so it always matches the Ksqlite release it was built from
- When a test runner is declared through the `ksqlite { wasm { } }` extension, generates a small
  Karma configuration (`karma.config.d/ksqlite.js`) that serves `ksqlite.wasm` as a static file
  and injects the environment variables `ksqlite-bootstrap.mjs` reads to locate it under Karma's
  serving root. Karma is the only supported runner for now

## Applying it

```kotlin
// webApp/build.gradle.kts
plugins {
    id("io.github.manriif.ksqlite") version "<version>"
}
```

Apply it in the final application module. Intermediate modules don't need it.

A module that runs Ksqlite-related tests is the exception, it needs the plugin too, with its test
runner declared:

```kotlin
// shared/build.gradle.kts
plugins {
    id("io.github.manriif.ksqlite") version "<version>"
}

ksqlite {
    wasm {
        testRunner = WasmTestRunner.Karma
    }
}
```
