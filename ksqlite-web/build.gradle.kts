import komple.exec.createCommandExecutor
import komple.task.doLastWhenOutputChanged
import komple.tool.KompleTool
import modules.compileSqliteWasm
import modules.copySqliteWasmGeneratedResources
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.opensavvy.resources.producer)
    alias(kompleLibs.plugins.komple)
}

val generatedResourcesDirectory =
    layout.buildDirectory.dir("generated/ksqlite/src/webMain/resources")

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
    val outputDirectory = generatedResourcesDirectory

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
        from(copyWasmResources.map { it.outputs.files })
    }

    compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
        compileTaskProvider.configure {
            dependsOn(compileWasm)
        }
    }
}