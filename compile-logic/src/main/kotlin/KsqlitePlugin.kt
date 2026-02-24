import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import tasks.registerRootTasks

/**
 * Plugin for Kotlin SQLite.
 */
class KsqlitePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension =
            target.extensions.create<KsqliteExtension>(KSQLITE_EXTENSION_NAME)

        target.registerRootTasks(extension)

        target.afterEvaluate {
            configureKsqliteSources(extension)
        }
    }
}