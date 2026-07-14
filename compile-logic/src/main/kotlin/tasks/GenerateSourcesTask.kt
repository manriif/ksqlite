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

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

/**
 * Base for task generating sources.
 */
@DisableCachingByDefault
abstract class GenerateSourcesTask : DefaultTask() {

    @get:Inject
    protected abstract val fileOperations: FileSystemOperations

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val libraryName: Property<String>

    /**
     * Writes [content] to this file, ensuring parent directories exists.
     */
    protected fun File.write(content: String) {
        val parent = parentFile ?: return writeText(content)

        if (!parent.exists()) {
            check(parent.mkdirs())
        }

        writeText(content)
    }
}