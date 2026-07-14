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
package ksqlite.gradle.wasm

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Generates the configuration to run ksqlite related test within Karma environment.
 */
@CacheableTask
internal abstract class GenerateKarmaConfigTask : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val jsFile = outputFile.get().asFile
            .apply { parentFile.mkdirs() }

        jsFile.writeText(
            """
                config.files.push({
                  pattern: "kotlin/ksqlite/ksqlite.wasm",
                  included: false,
                  served: true,
                  watched: false
                });
        
                config.client = config.client || {};
                config.client.env = config.client.env || {};
        
                config.client.env.ksqlite = {
                    isTest: true,
                    prefix: "base/kotlin/ksqlite/"
                }
            """.trimIndent()
        )
    }
}