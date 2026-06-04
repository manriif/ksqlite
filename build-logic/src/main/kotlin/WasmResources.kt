import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.HasConfigurableAttributes
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering

const val WASM_RESOURCES_CONFIGURATION = "wasmResourcesElements"
const val WASM_RESOURCES_ATTRIBUTE_NAME = "ksqlite.wasm.resources"

private val wasmResourcesAttribute = Attribute.of(WASM_RESOURCES_ATTRIBUTE_NAME, String::class.java)

/**
 * Applies the attributes for wasm resources configuration to [this@applyWasmResourcesAttributes].
 */
fun HasConfigurableAttributes<*>.applyWasmResourcesAttributes() {
    attributes {
        attribute(wasmResourcesAttribute, "true")
    }
}

/**
 * Registers a configuration resolving wasm resources from [ksqliteWeb] project then registers and
 * returns a task extracting those resources.
 */
@Suppress("UnstableApiUsage")
fun Project.registerExtractWasmResourcesTask(ksqliteWeb: ProjectDependency): TaskProvider<Sync> {
    val wasmResourcesDependencies by configurations.registering {
        isCanBeConsumed = false
        isCanBeResolved = false
        isCanBeDeclared = true
    }

    val wasmResources by configurations.resolvable("wasmResources") {
        extendsFrom(wasmResourcesDependencies)
        applyWasmResourcesAttributes()
    }

    dependencies {
        wasmResourcesDependencies(
            project(
                mapOf(
                    "path" to ksqliteWeb.path,
                    "configuration" to WASM_RESOURCES_CONFIGURATION
                )
            )
        )
    }

    return tasks.register("extractWasmResources", Sync::class) {
        dependsOn(wasmResources.buildDependencies)
        from(wasmResources.map(::zipTree))
        into(layout.buildDirectory.dir("extracted/ksqlite/wasm/resources"))
    }
}