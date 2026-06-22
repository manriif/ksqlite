import komple.tool.KompleTool
import komple.task.enableTracking
import komple.task.clearAndGetAsFile
import modules.copySqliteWasmGeneratedResources
import org.gradle.kotlin.dsl.support.serviceOf
import modules.WasmCompileTask

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val compileWasm by tasks.registeringKsqliteTracked<WasmCompileTask> { tracker ->
    this.tracker = tracker
    ksqliteDirectory = ksqlite.ksqliteDirectory
    sqliteDirectory = ksqlite.sqliteDirectory
    outputDirectory = layout.buildDirectory.dir("ksqlite/wasm")
    execEnvironment = komple.execEnvironments.wasm

    val requiredTools = with(komple.tools) {
        listOf(emscripten, gnuSed, wabt)
            .map(KompleTool::installTaskProvider)
            .toTypedArray()
    }

    dependsOn(*requiredTools)
    tracker.enableTracking()
}

val copyWasmResources by tasks.registeringKsqlite {
    val fileOperations = serviceOf<FileSystemOperations>()
    val inputDirectory = compileWasm.map { it.outputs.files.singleFile }
    val outputDirectory = layout.buildDirectory.dir("generated/ksqlite/resources")

    inputs.dir(inputDirectory)
    outputs.dir(outputDirectory)

    doLast {
        copySqliteWasmGeneratedResources(
            fileOperations = fileOperations,
            inputDirectory = inputDirectory.get(),
            outputDirectory = fileOperations.clearAndGetAsFile(outputDirectory)
        )
    }
}

@Suppress("TaskMissingDescription")
val zipWasmResources by tasks.registering(Zip::class) {
    from(copyWasmResources)
    archiveClassifier = "resources"
}

@Suppress("UnstableApiUsage")
val wasmResources by configurations.consumable(WASM_RESOURCES_CONFIGURATION) {
    applyWasmResourcesAttributes()
}

artifacts {
    add(wasmResources.name, zipWasmResources)
}

kotlin {
    webTargets()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
        }

        webMain {
            dependencies {
                api(libs.kotlin.wrappers.js)
            }
        }
    }
}