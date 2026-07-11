@file:Suppress("UnstableApiUsage")

package ksqlite.gradle.wasm

import ksqlite.gradle.registerKsqlite
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

private val WasmResourcesAttributeTargetName =
    Attribute.of("ksqlite.wasm.resources.targetName", String::class.java)

/**
 * Configures WASM resources for web targets.
 */
internal fun Project.extractWasmResources(
    wasm: KsqliteWasm,
    ksqliteVersion: String
) {
    dependencies {
        attributesSchema {
            attribute(WasmResourcesAttributeTargetName)
        }
    }

    val shadowTestRunnerTaskProvider = tasks.register("ksqliteConfigureTestRunner")

    extensions.getByType<KotlinMultiplatformExtension>().targets.configureEach {
        if (this is KotlinJsTargetDsl) {
            configureWasmResources(
                wasm = wasm,
                ksqliteVersion = ksqliteVersion,
                shadowTestRunnerTaskProvider = shadowTestRunnerTaskProvider
            )
        }
    }

    project.afterEvaluate {
        wasm.testRunner.orNull?.let { runner ->
            tasks.registerTestRunnerTask(
                runner = runner,
                shadowTestRunnerTaskProvider = shadowTestRunnerTaskProvider
            )
        }
    }
}

/**
 * Configures WASM resources for this [KotlinJsTargetDsl].
 */
@Suppress("UnstableApiUsage")
private fun KotlinJsTargetDsl.configureWasmResources(
    wasm: KsqliteWasm,
    ksqliteVersion: String,
    shadowTestRunnerTaskProvider: TaskProvider<*>
) {
    val postfix = targetName.uppercaseFirstChar()
    val configName = "ksqliteWasmResources$postfix"

    val wasmResourcesBase = project.configurations.register("${configName}Base") {
        isCanBeConsumed = false
        isCanBeResolved = false
        isCanBeDeclared = true
    }

    val wasmResources by project.configurations.resolvable(configName) {
        extendsFrom(wasmResourcesBase)

        attributes {
            attribute(WasmResourcesAttributeTargetName, targetName)
        }
    }

    project.dependencies {
        wasmResourcesBase("io.github.manriif.ksqlite:ksqlite-wasm-resources:$ksqliteVersion")
    }

    val extractResources = project.tasks.registerKsqlite<Sync>("wasmResources${postfix}Extract") {
        description = "Extracts the Ksqlite WASM resources for the $targetName target"

        val extractDirectory = project.layout.buildDirectory
            .dir("ksqlite/wasm/resources/${targetName.lowercase()}")

        val archiveOperations = project.serviceOf<ArchiveOperations>()

        from(wasmResources.map(archiveOperations::zipTree))
        into(extractDirectory)

        dependsOn(shadowTestRunnerTaskProvider)
    }

    compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
        project.tasks.named<ProcessResources>(processResourcesTaskName) {
            from(extractResources)
        }
    }
}

/**
 * Registers the task for [runner] configuration.
 */
private fun TaskContainer.registerTestRunnerTask(
    runner: WasmTestRunner,
    shadowTestRunnerTaskProvider: TaskProvider<*>
) {
    val taskProvider = when (runner) {
        Karma -> registerKsqlite<GenerateKarmaConfigTask>("configureKarmaTestRunner") {
            description = "Generates Karma configuration for Ksqlite"
            outputFile = project.layout.projectDirectory.file("karma.config.d/ksqlite.js")
        }
    }

    shadowTestRunnerTaskProvider.configure {
        dependsOn(taskProvider)
    }
}