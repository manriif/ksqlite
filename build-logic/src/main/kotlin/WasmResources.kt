import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.HasConfigurableAttributes
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

const val WASM_RESOURCES_CONFIGURATION = "wasmResourcesElements"
const val WASM_RESOURCES_ATTRIBUTE_NAME = "ksqlite.wasm.resources"

private val wasmResourcesAttribute = Attribute.of(WASM_RESOURCES_ATTRIBUTE_NAME, String::class.java)

/**
 * Applies the attributes for WASM resources configuration to [this@applyWasmResourcesAttributes].
 */
fun HasConfigurableAttributes<*>.applyWasmResourcesAttributes() {
    attributes {
        attribute(wasmResourcesAttribute, "true")
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

    val wasmResourcesDeps = project.configurations.register("wasmResourceDependencies$postfix") {
        isCanBeConsumed = false
        isCanBeResolved = false
        isCanBeDeclared = true
    }

    val wasmResources by project.configurations.resolvable("wasmResources$postfix") {
        extendsFrom(wasmResourcesDeps)
        applyWasmResourcesAttributes()
    }

    project.dependencies {
        wasmResourcesDeps(
            project(
                mapOf(
                    "path" to ksqliteWeb.path,
                    "configuration" to WASM_RESOURCES_CONFIGURATION
                )
            )
        )
    }

    val extractResourceTask = project.tasks.register(
        name = "extractWasmResources$postfix",
        type = Sync::class
    ) {
        dependsOn(wasmResources.buildDependencies)

        val extractDirectory = project.layout.buildDirectory
            .dir("extracted/ksqlite/wasm/resources/${targetName.lowercase()}")

        val archiveOperations = project.serviceOf<ArchiveOperations>()

        from(wasmResources.map(archiveOperations::zipTree)) {
            include { !it.name.endsWith("-prod.mjs") }
            rename { it.replace("-test", "") }
        }

        into(extractDirectory)
    }

    compilations.named(KotlinCompilation.TEST_COMPILATION_NAME).configure {
        defaultSourceSet.resources.srcDir(extractResourceTask)
    }
}