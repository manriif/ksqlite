/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    if (ksqlite.build.isWasmEnabled) {
        from(copyWasmResources)
    }

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
 * FIXME: consider another approach to bring wasm resources to consumers. Kotlin does bundle
 *   plain source set resources straight into the published klib, but there is no supported
 *   Gradle API for a downstream project to pull them back out of it. There is no reason to leave
 *   the Kotlin ecosystem just for two or three poor files.
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
    val wasmResources = configurations.consumable(wasmResourcesConfigurationName(targetName)) {
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