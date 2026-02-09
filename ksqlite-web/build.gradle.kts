@file:Suppress("HasPlatformType")

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import tasks.registerSqliteCompileWasmTask
import tasks.registerSqliteCopyWasmResourcesTask

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.opensavvy.resources.producer)
}

val generatedResourcesDirectory =
    layout.buildDirectory.dir("generated/ksqlite/src/webMain/resources")

val sqliteCopyWasmResourcesTaskProvider = registerSqliteCopyWasmResourcesTask(
    wasmCompileTaskProvider = registerSqliteCompileWasmTask(
        outputDirectory = layout.buildDirectory.dir("sqlite/wasm")
    ),
    outputDirectory = generatedResourcesDirectory
)

kotlin {
    webTargets().forEach { target ->
        target.configureJsTarget()
    }

    sourceSets.webMain {
        resources.srcDir(generatedResourcesDirectory)
    }
}

fun KotlinJsTargetDsl.configureJsTarget() {
    tasks.named("${name}ResourceArchive") {
        dependsOn(sqliteCopyWasmResourcesTaskProvider)
    }

    compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
        compileTaskProvider.configure {
            dependsOn(sqliteCopyWasmResourcesTaskProvider)
        }
    }
}