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