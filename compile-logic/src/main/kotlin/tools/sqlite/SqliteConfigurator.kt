/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.sqlite

import komple.platform.Host
import komple.task.enableTracking
import komple.task.integrity.DigestAlgorithm
import komple.tool.configurator.DefaultKompleToolConfigurator
import komple.tool.extension.ExtensionConfigurationScope
import komple.tool.extension.createExtension
import komple.tool.task.DownloadTaskRegistrationScope
import komple.tool.task.ExtractTaskRegistrationScope
import komple.tool.task.InstallTaskRegistrationScope
import komple.tool.task.IntegrityTaskRegistrationScope
import komple.tool.task.checksum
import komple.tool.task.install
import komple.tool.task.unzip
import komple.tool.task.url
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import javax.inject.Inject

/**
 * Configurator for SQLite.
 */
abstract class SqliteConfigurator @Inject constructor(toolName: String) :
    DefaultKompleToolConfigurator<SqliteExtension>(toolName) {

    override fun supportHost(host: Host): Boolean = when (host.operatingSystem) {
        Linux, MacOS, Windows -> true
    }

    override fun ExtensionConfigurationScope<SqliteExtension>.configureExtension(): SqliteExtension {
        return createExtension()
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

    override fun IntegrityTaskRegistrationScope<SqliteExtension>.registerIntegrityTask(): TaskProvider<*> {
        return checksum(extension.checksum, DigestAlgorithm.SHA3_256)
    }

    override fun ExtractTaskRegistrationScope<SqliteExtension>.registerExtractTask(): TaskProvider<*> {
        return unzip(true)
    }

    override fun InstallTaskRegistrationScope<SqliteExtension>.registerInstallTask(): TaskProvider<*> {
        return install<SqliteInstallTask> { context ->
            ksqliteDirectory = extension.ksqliteDirectory
            sqliteMcDirectory = extension.sqliteMcDirectory

            context.tracker.enableTracking()
        }
    }
}