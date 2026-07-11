import komple.KompleRootExtension
import komple.registerTool
import komple.tool.KompleToolPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import tools.sqlite.SqliteConfigurator
import tools.sqlitemc.SqliteMCConfigurator

/**
 * Plugin for Kotlin SQLite.
 */
class KsqlitePlugin : KompleToolPlugin() {

    override fun configure(project: Project, komple: KompleRootExtension) {
        komple.registerTool<SqliteMCConfigurator>("Sqlite Multiple Ciphers")
        komple.registerTool<SqliteConfigurator>("Sqlite")

        val extension = project.extensions.create<KsqliteExtension>(KSQLITE_EXTENSION_NAME)

        project.allprojects {
            if (this != project) {
                extensions.add(KSQLITE_EXTENSION_NAME, extension)
            }
        }
    }
}