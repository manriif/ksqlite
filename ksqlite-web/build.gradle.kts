import komple.exec.createCommandExecutor
import komple.task.doLastWhenOutputChanged
import komple.tool.KompleTool
import modules.compileSqliteWasm
import modules.copySqliteWasmGeneratedResources
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val compileWasm by tasks.registeringKsqlite { context ->
    val requiredTools = komple.tools.run {
        listOf(emscripten, gnuSed, wabt)
            .map(KompleTool::installTaskProvider)
            .toTypedArray()
    }

    dependsOn(*requiredTools)

    val execEnvironment = komple.execEnvironments.wasm
    val fileOperations = serviceOf<FileSystemOperations>()
    val execOperations = serviceOf<ExecOperations>()
    val ksqliteDirectory = ksqlite.ksqliteDirectory
    val sqliteDirectory = ksqlite.sqliteDirectory
    val outputDirectory = layout.buildDirectory.dir("ksqlite/wasm")

    inputs.dir(sqliteDirectory)
    outputs.dir(outputDirectory)

    doLastWhenOutputChanged(context) {
        compileSqliteWasm(
            fileOperations = fileOperations,
            commandExecutor = execEnvironment.createCommandExecutor(execOperations),
            ksqliteDirectory = ksqliteDirectory.get().asFile,
            sqliteDirectory = sqliteDirectory.get().asFile,
            outputDirectory = fileOperations.clearAndGetFile(outputDirectory)
        )
    }
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
            outputDirectory = fileOperations.clearAndGetFile(outputDirectory)
        )
    }
}

val zipWasmResources by tasks.registering(Zip::class) {
    from(copyWasmResources)
    archiveClassifier = "resources"
}

@Suppress("UnstableApiUsage")
val wasmResources by configurations.consumable(WASM_RESOURCES_CONFIG_NAME_PRODUCER) {
    applyWasmResourcesAttributes(this)
}

artifacts {
    add(wasmResources.name, zipWasmResources)
}

publishing {
    publications {
        register<MavenPublication>("wasmResources") {
            artifact(zipWasmResources)
        }
    }
}

kotlin {
    webTargets()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
        }

        webMain {
            dependencies {
                api(libs.kotlin.js)
            }
        }
    }
}