# Module Ksqlite WASM Resources

Compiles `ksqlite` to WebAssembly and packages the result for Kotlin/JS and Kotlin/Wasm targets.

## What's produced

- `ksqlite.wasm` / `ksqlite.mjs`: compiled against Ksqlite's own amalgamation through the `wasm`
  execution environment (Emscripten, GNU sed, wabt), registered in the root `build.gradle.kts`
  using [Komple](https://github.com/manriif/komple). This is what `ksqlite-foreign`'s `wasm`
  module, the Kotlin binding layer, binds to
- [`ksqlite-bootstrap.mjs`](src/webMain/resources/ksqlite/ksqlite-bootstrap.mjs): hand-written glue
  that the `wasm` foreign module imports at runtime. It
  initializes the Emscripten module, exports the resulting `sqlite3` instance, and switches on
  debug logging plus adjusts the WASM lookup path when running under Karma, the only supported
  runner for now

Kotlin bundles plain resources like the bootstrap module straight into the published klib, but
exposes no supported API for a downstream project to pull them back out, and the compiled
`ksqlite.wasm`/`ksqlite.mjs` never go through that path at all. Instead, all three files are
zipped together and published as an extra, per-web-target Maven variant of this module, through
Kotlin's internal publishing API, on top of the target's usual published variants.

## Consuming these resources

- **Downstream projects** use `ksqlite-gradle-plugin`: it resolves the variant, extracts it into
  the consuming project, and generates the Karma configuration (`karma.config.d/ksqlite.js`)
  needed to run JS/Wasm tests
- **Within this repository**, `ksqlite-gradle-plugin` isn't applied, so every module that runs web
  tests (`ksqlite-capi`, `ksqlite-kapi`, and future ones) instead:
    - Resolves the per-web-target variant itself as a project dependency, extracted and wired into
      its test compilation as generated resources by `WasmResources.kt` in `build-logic`
    - Carries its own hand-copied `karma.config.d/ksqlite.js`, see `ksqlite-capi` for an example
