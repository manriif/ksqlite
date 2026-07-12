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
package ksqlite.gradle

import ksqlite.gradle.wasm.extractWasmResources
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * Ksqlite Gradle Plugin.
 */
public class KsqlitePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions
            .create<KsqliteExtension>("ksqlite")
            .apply { configure() }

        val ksqliteVersion = this::class.java.classLoader
            .getResource("version.txt")
            ?.readText()
            ?.trim()
            ?: error("Failed to read Ksqlite version")

        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            target.extractWasmResources(extension.wasm, ksqliteVersion)
        }
    }
}