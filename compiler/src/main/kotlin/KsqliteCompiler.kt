import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName

/**
 * Name of the sqlite compiler extension.
 */
const val KSQLITE_COMPILER_EXTENSION_NAME = "ksqliteCompiler"

/**
 * Retrieves and returns the [KsqliteCompilerExtension] from root project.
 */
val Project.ksqliteCompilerExtension: KsqliteCompilerExtension
    get() = rootProject.extensions.getByName<KsqliteCompilerExtension>(KSQLITE_COMPILER_EXTENSION_NAME)