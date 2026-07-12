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
package tools.sqlitemc

import komple.platform.Host
import komple.task.integrity.DigestAlgorithm
import komple.tool.configurator.DefaultKompleToolConfigurator
import komple.tool.extension.ExtensionConfigurationScope
import komple.tool.extension.createExtension
import komple.tool.task.DownloadTaskRegistrationScope
import komple.tool.task.ExtractTaskRegistrationScope
import komple.tool.task.IntegrityTaskRegistrationScope
import komple.tool.task.checksum
import komple.tool.task.unzip
import komple.tool.task.url
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

/**
 * Configurator for SQLite Multiple Ciphers.
 */
abstract class SqliteMCConfigurator @Inject constructor(toolName: String) :
    DefaultKompleToolConfigurator<SqliteMCExtension>(toolName) {

    override fun supportHost(host: Host): Boolean = when (host.operatingSystem) {
        Linux, MacOS, Windows -> true
    }

    override fun ExtensionConfigurationScope<SqliteMCExtension>.configureExtension(): SqliteMCExtension {
        return createExtension()
    }

    override fun DownloadTaskRegistrationScope<SqliteMCExtension>.registerDownloadTask(): TaskProvider<*> {
        return url(extension.version.zip(extension.sqliteVersion) { sqliteMCVersion, sqliteVersion ->
            "https://github.com/utelle/SQLite3MultipleCiphers/releases/download/v$sqliteMCVersion" +
                    "/sqlite3mc-$sqliteMCVersion-sqlite-$sqliteVersion-amalgamation.zip"
        })
    }

    override fun IntegrityTaskRegistrationScope<SqliteMCExtension>.registerIntegrityTask(): TaskProvider<*> {
        return checksum(extension.checksum, DigestAlgorithm.SHA_256)
    }

    override fun ExtractTaskRegistrationScope<SqliteMCExtension>.registerExtractTask(): TaskProvider<*> {
        return unzip(false)
    }
}