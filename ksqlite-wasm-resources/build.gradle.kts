@file:Suppress("UnstableApiUsage")

import komple.task.clearAndGetAsFile
import komple.task.enableTracking
import komple.tool.KompleTool
import modules.copySqliteWasmGeneratedResources
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import tasks.WasmCompileTask
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val compileWasm = tasks.registerKsqliteTracked<WasmCompileTask>("compileWasm") { tracker ->
    this.tracker = tracker
    sqliteVersion = libs.versions.sqlite
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

val copyWasmResources = tasks.registerKsqlite<DefaultTask>("copyWasmResources") {
    val fileOperations = serviceOf<FileSystemOperations>()
    val inputDirectory = compileWasm.map { it.outputs.files.singleFile }
    val outputDirectory = layout.buildDirectory.dir("generated/ksqlite/resources")

    inputs.dir(inputDirectory)
    outputs.dir(outputDirectory)

    doLast {
        copySqliteWasmGeneratedResources(
            fileOperations = fileOperations,
            inputDirectory = inputDirectory.get(),
            outputDirectory = fileOperations
                .clearAndGetAsFile(outputDirectory)
                .resolve("ksqlite")
        )
    }
}

val zipWasmResources = tasks.registerKsqlite<Zip>("zipWasmResources") {
    from(copyWasmResources)
    archiveClassifier = "resources"
}

kotlin {
    webTargets().forEach { target ->
        target.configureWebTarget()
    }

    zipWasmResources.configure {
        from(sourceSets.webMain.map { it.resources })
    }
}

/**
 * A little bit ugly but it works.
 *
 * FIXME: consider another approach to bring wasm resources to consumer, Kotlin doesn't want us
 *   taking up a few bytes in the publications; what's more, they also bundle the resources into
 *   the klib. There is no reason to leave the Kotlin ecosystem just for two or three poor files.
 */
@Suppress("UNCHECKED_CAST")
@OptIn(InternalKotlinGradlePluginApi::class) // :-)
val adhocField = run {
    val property = Class
        .forName("org.jetbrains.kotlin.gradle.plugin.mpp.KotlinTargetSoftwareComponentImpl")
        .kotlin
        .memberProperties
        .firstOrNull { it.name == "adhocComponent" }
        ?: error("Could not access KotlinTargetSoftwareComponentImpl.adhocComponent")

    property.isAccessible = true
    property as KProperty1<Any, AdhocComponentWithVariants>
}

fun KotlinJsTargetDsl.configureWebTarget() {
    val wasmResources by configurations.consumable(wasmResourcesConfigurationName(targetName)) {
        applyWasmResourcesAttributes(targetName)
    }

    artifacts {
        add(wasmResources.name, zipWasmResources)
    }

    components.forEach { component ->
        afterEvaluate {
            adhocField.get(component).addVariantsFromConfiguration(wasmResources) {
                mapToMavenScope("runtime")
                mapToOptional()
            }
        }
    }
}