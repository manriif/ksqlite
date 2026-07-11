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