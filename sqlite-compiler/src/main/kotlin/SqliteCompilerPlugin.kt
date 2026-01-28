import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class SqliteCompilerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension =
            target.extensions.create<SqliteCompilerExtension>(SQLITE_COMPILER_EXTENSION_NAME)

        target.registerTasks(extension)
    }
}