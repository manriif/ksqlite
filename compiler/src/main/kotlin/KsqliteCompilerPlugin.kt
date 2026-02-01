import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import tasks.registerTasks

/**
 * Plugin for SQLite compilation.
 */
class KsqliteCompilerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension =
            target.extensions.create<KsqliteCompilerExtension>(KSQLITE_COMPILER_EXTENSION_NAME)

        target.registerTasks(extension)
    }
}