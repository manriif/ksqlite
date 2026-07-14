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
package tasks

import komple.project.c.CCompilation
import komple.task.clearAndGetAsFile
import modules.createKsqliteFfmRuntimeMetadataContent
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Task responsible for generating the FFM sources.
 */
@DisableCachingByDefault
abstract class GenerateFfmSourcesTask : GenerateSourcesTask() {

    @get:Internal
    abstract val compilations: ListProperty<CCompilation>

    @TaskAction
    fun generate() {
        val outputDirectory = fileOperations.clearAndGetAsFile(outputDirectory)
        val packageName = packageName.get()

        val content = createKsqliteFfmRuntimeMetadataContent(
            packageName = packageName,
            libraryName = libraryName.get(),
            compilations = compilations.get()
        )

        outputDirectory
            .resolve("$packageName/KsqliteFfmGenerated.kt")
            .write(content)
    }
}