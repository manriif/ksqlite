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
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.HasConfigurableAttributes
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

private const val WASM_RESOURCES_CONFIGURATION = "ksqliteWasmResources"

private val wasmResourcesAttributeTargetName =
    Attribute.of("ksqlite.wasm.resources.targetName", String::class.java)

/**
 * Name of the WASM resources consumable configuration.
 */
fun wasmResourcesConfigurationName(targetName: String): String =
    "${WASM_RESOURCES_CONFIGURATION}${targetName.uppercaseFirstChar()}"

/**
 * Applies the attributes for WASM resources configuration to [this@applyWasmResourcesAttributes].
 */
fun HasConfigurableAttributes<*>.applyWasmResourcesAttributes(targetName: String) {
    attributes {
        attribute(wasmResourcesAttributeTargetName, targetName)
    }
}

/**
 * Configures WASM resources for web targets.
 */
fun KotlinMultiplatformExtension.configureWasmResources(ksqliteWeb: ProjectDependency) {
    targets.configureEach {
        if (this is KotlinJsTargetDsl) {
            configureWasmResources(ksqliteWeb)
        }
    }
}

/**
 * Configures WASM resources for this [KotlinJsTargetDsl].
 */
@Suppress("UnstableApiUsage")
private fun KotlinJsTargetDsl.configureWasmResources(ksqliteWeb: ProjectDependency) {
    val postfix = targetName.uppercaseFirstChar()
    val configName = "ksqliteWasmResources$postfix"

    val wasmResourcesBase = project.configurations.register("${configName}Base") {
        isCanBeConsumed = false
        isCanBeResolved = false
        isCanBeDeclared = true
    }

    val wasmResources = project.configurations.resolvable(configName) {
        extendsFrom(wasmResourcesBase)
        applyWasmResourcesAttributes(targetName)
    }

    project.dependencies {
        wasmResourcesBase(
            project(
                mapOf(
                    "path" to ksqliteWeb.path,
                    "configuration" to wasmResourcesConfigurationName(targetName)
                )
            )
        )
    }

    val extractResourceTask = project.tasks.register<Sync>("extractWasmResources$postfix") {
        dependsOn(wasmResources.map(ResolvableConfiguration::getBuildDependencies))

        val extractDirectory = project.layout.buildDirectory
            .dir("ksqlite/wasm/resources/${targetName.lowercase()}")

        val archiveOperations = project.serviceOf<ArchiveOperations>()

        from(wasmResources.map { it.map(archiveOperations::zipTree) })
        into(extractDirectory)
    }

    compilations.named(KotlinCompilation.TEST_COMPILATION_NAME).configure {
        project.tasks.named<ProcessResources>(processResourcesTaskName) {
            from(extractResourceTask)
        }
    }
}