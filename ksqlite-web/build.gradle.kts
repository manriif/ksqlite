@file:Suppress("HasPlatformType")

import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import tasks.registerSqliteCompileWasmTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

val compiledSqliteDirectory = layout.buildDirectory.dir("sqlite/wasm")

val sqliteCompileWasmTaskProvider = registerSqliteCompileWasmTask(
    outputDirectory = compiledSqliteDirectory
)

kotlin {
    webTargets().forEach { target ->
        target.configureJsTarget()
    }
}

fun KotlinJsTargetDsl.configureJsTarget() {
    compilations.configureEach {
        compileTaskProvider.configure {
            dependsOn(sqliteCompileWasmTaskProvider)
        }

        tasks.named<ProcessResources>(processResourcesTaskName).apply {
            configure {
                dependsOn(sqliteCompileWasmTaskProvider)

                from(compiledSqliteDirectory.map { "esm64" }) {
                    into(".")
                }

                from(compiledSqliteDirectory) {
                    include { element ->
                        element.name == "sqlite3-opfs-async-proxy.js"
                                || element.name == "sqlite3-worker1.mjs"
                                || element.name == "sqlite3-worker1-promiser.mjs"
                                || element.name == "sqlite3-worker1-promiser-bundler-friendly.mjs"
                    }

                    into(".")
                }
            }
        }
    }
}