@file:Suppress("HasPlatformType")

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import tasks.registerSqliteCompileWasmTask

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.opensavvy.resources.producer)
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
    tasks.named("${name}ResourceArchive") {
        dependsOn(sqliteCompileWasmTaskProvider)
    }

    compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
        compileTaskProvider.configure {
            dependsOn(sqliteCompileWasmTaskProvider)
        }

        defaultSourceSet.resources.srcDir(compiledSqliteDirectory)

        // TODO clean in generated resource directory
        /*tasks.named<ProcessResources>(processResourcesTaskName).apply {
            configure {
                dependsOn(sqliteCompileWasmTaskProvider)

                from(compiledSqliteDirectory.map { it.dir("esm64") }) {
                    into(".")
                }

                from(compiledSqliteDirectory) {
                    include { element ->
                        element.name == "sqlite3.js"
                                || element.name == "sqlite3-opfs-async-proxy.js"
                                || element.name == "sqlite3-worker1.mjs"
                                || element.name == "sqlite3-worker1-promiser.mjs"
                                || element.name == "sqlite3-worker1-promiser-bundler-friendly.mjs"
                    }

                    into(".")
                }
            }
        }*/
    }
}