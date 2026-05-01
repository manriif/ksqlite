import komple.exec.createCommandExecutor
import komple.task.doLastWhenOutputChanged
import modules.compileSqliteWasm
import modules.copySqliteWasmGeneratedResources
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import utils.clearAndGetFile

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.opensavvy.resources.producer)
    alias(kompleLibs.plugins.komple)
}

val generatedResourcesDirectory =
    layout.buildDirectory.dir("generated/ksqlite/src/webMain/resources")

val compileSqliteWasm by tasks.registeringKsqlite { context ->
    val execEnvironment = komple.execEnvironments.wasm
    val fileOperations = serviceOf<FileSystemOperations>()
    val execOperations = serviceOf<ExecOperations>()
    val sqliteDirectory = ksqlite.sqliteDirectory
    val outputDirectory = layout.buildDirectory.dir("sqlite/wasm")

    inputs.dir(sqliteDirectory)
    outputs.dir(outputDirectory)

    doLastWhenOutputChanged(context) {
        compileSqliteWasm(
            fileOperations = fileOperations,
            commandExecutor = execEnvironment.createCommandExecutor(execOperations),
            sqliteDirectory = sqliteDirectory.get().asFile,
            outputDirectory = fileOperations.clearAndGetFile(outputDirectory)
        )
    }
}

val copySqliteWasmResources by tasks.registeringKsqlite {
    val fileOperations = serviceOf<FileSystemOperations>()
    val wasmDirectory = compileSqliteWasm.map { it.outputs.files.singleFile }
    val outputDirectory = generatedResourcesDirectory

    inputs.dir(wasmDirectory)
    outputs.dir(outputDirectory)

    doLast {
        copySqliteWasmGeneratedResources(
            fileOperations = fileOperations,
            wasmDirectory = wasmDirectory.get(),
            outputDirectory = fileOperations.clearAndGetFile(outputDirectory)
        )
    }
}

kotlin {
    webTargets().forEach { target ->
        target.configureJsTarget()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
        }

        webMain {
            resources.srcDir(generatedResourcesDirectory)
        }
    }
}

fun KotlinJsTargetDsl.configureJsTarget() {
    tasks.named<Zip>("${name}ResourceArchive") {
        from(copySqliteWasmResources.map { it.outputs.files })
    }

    compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
        compileTaskProvider.configure {
            dependsOn(compileSqliteWasm)
        }
    }
}