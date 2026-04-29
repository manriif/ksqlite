package tools.sqlite

import komple.platform.Host
import komple.tool.configurator.DefaultKompleToolConfigurator
import komple.tool.extension.ExtensionConfigurationScope
import komple.tool.extension.createExtension
import komple.tool.task.Algorithm
import komple.tool.task.DownloadTaskRegistrationScope
import komple.tool.task.ExtractTaskRegistrationScope
import komple.tool.task.InstallTaskRegistrationScope
import komple.tool.task.IntegrityTaskRegistrationScope
import komple.tool.task.checksum
import komple.tool.task.doLastWhenOutputChanged
import komple.tool.task.register
import komple.tool.task.unzip
import komple.tool.task.url
import modules.configureSqliteWasmTrunk
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.support.serviceOf
import javax.inject.Inject

/**
 * Configurator for Sqlite.
 */
abstract class SqliteConfigurator @Inject constructor(toolName: String) :
    DefaultKompleToolConfigurator<SqliteExtension>(toolName) {

    override fun supportHost(host: Host): Boolean = when (host.operatingSystem) {
        Linux, MacOS, Windows -> true
    }

    override fun DownloadTaskRegistrationScope<SqliteExtension>.registerDownloadTask(): TaskProvider<*> {
        return url(extension.version.zip(extension.releaseYear) { version, releaseYear ->
            val components = version.split('.')
            val (major, minor, patch) = components
            val build = components.getOrElse(3) { "0" }

            val normalizedVersion = "%s%s%s%s".format(
                major,
                minor.padStart(2, '0'),
                patch.padStart(2, '0'),
                build.padStart(2, '0'),
            )

            "https://www.sqlite.org/$releaseYear/sqlite-src-$normalizedVersion.zip"
        })
    }

    override fun ExtensionConfigurationScope<SqliteExtension>.configureExtension(): SqliteExtension {
        return createExtension()
    }

    override fun IntegrityTaskRegistrationScope<SqliteExtension>.registerIntegrityTask(): TaskProvider<*> {
        return checksum(extension.checksum, Algorithm.SHA3_256)
    }

    override fun ExtractTaskRegistrationScope<SqliteExtension>.registerExtractTask(): TaskProvider<*> {
        return unzip(true)
    }

    override fun InstallTaskRegistrationScope<SqliteExtension>.registerInstallTask(): TaskProvider<*> {
        return register<DefaultTask> { context ->
            val fileOperations = project.serviceOf<FileSystemOperations>()
            val ksqliteDirectory = extension.ksqliteDirectory
            val sqliteMcDirectory = extension.sqliteMcDirectory

            inputs.dir(ksqliteDirectory)
            inputs.dir(sqliteMcDirectory)

            doLastWhenOutputChanged(context) {
                fileOperations.copy {
                    from(sqliteMcDirectory)
                    from(context.extractDirectory.directory)
                    into(context.outputDirectory)
                }

                configureSqliteWasmTrunk(
                    ksqliteDirectory = ksqliteDirectory.get().asFile,
                    sqliteDirectory = context.outputDirectory.asFile
                )
            }
        }
    }
}