import tasks.registerSqliteCompileWasmTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

registerSqliteCompileWasmTask(layout.buildDirectory.dir("sqlite/wasm"))

kotlin {
    webTargets()
}