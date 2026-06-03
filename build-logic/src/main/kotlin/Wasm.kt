import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.HasConfigurableAttributes
import org.gradle.api.attributes.LibraryElements

const val WASM_RESOURCES_CONFIG_NAME_PRODUCER = "wasmResourcesElements"
const val WASM_RESOURCES_CONFIG_NAME_CONSUMER = "wasmResources"

/**
 * Applies the attributes for wasm resources configuration to [container].
 */
fun Project.applyWasmResourcesAttributes(container: HasConfigurableAttributes<*>) {
    container.attributes {
        attribute(
            Category.CATEGORY_ATTRIBUTE,
            objects.named(Category::class.java, Category.LIBRARY)
        )

        attribute(
            LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
            objects.named(LibraryElements::class.java, LibraryElements.RESOURCES)
        )
    }
}