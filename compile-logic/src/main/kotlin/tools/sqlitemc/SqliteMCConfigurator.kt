package tools.sqlitemc

import komple.platform.Host
import komple.tool.configurator.DefaultKompleToolConfigurator
import komple.tool.extension.ExtensionConfigurationScope
import komple.tool.extension.createExtension
import komple.tool.task.Algorithm
import komple.tool.task.DownloadTaskRegistrationScope
import komple.tool.task.ExtractTaskRegistrationScope
import komple.tool.task.IntegrityTaskRegistrationScope
import komple.tool.task.checksum
import komple.tool.task.unzip
import komple.tool.task.url
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

/**
 * Configurator for Sqlite Multiple Ciphers.
 */
abstract class SqliteMCConfigurator @Inject constructor(toolName: String) :
    DefaultKompleToolConfigurator<SqliteMCExtension>(toolName) {

    override fun supportHost(host: Host): Boolean = when (host.operatingSystem) {
        Linux, MacOS, Windows -> true
    }

    override fun DownloadTaskRegistrationScope<SqliteMCExtension>.registerDownloadTask(): TaskProvider<*> {
        return url(extension.version.zip(extension.sqliteVersion) { sqliteMCVersion, sqliteVersion ->
            "https://github.com/utelle/SQLite3MultipleCiphers/releases/download/v$sqliteMCVersion" +
                    "/sqlite3mc-$sqliteMCVersion-sqlite-$sqliteVersion-amalgamation.zip"
        })
    }

    override fun ExtensionConfigurationScope<SqliteMCExtension>.configureExtension(): SqliteMCExtension {
        return createExtension()
    }

    override fun IntegrityTaskRegistrationScope<SqliteMCExtension>.registerIntegrityTask(): TaskProvider<*> {
        return checksum(extension.checksum, Algorithm.SHA_256)
    }

    override fun ExtractTaskRegistrationScope<SqliteMCExtension>.registerExtractTask(): TaskProvider<*> {
        return unzip(false)
    }
}