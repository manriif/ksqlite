package tasks

import komple.project.c.CProject
import komple.task.clearAndGetAsFile
import modules.createSqliteJniRuntimeMetadataContent
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Task responsible for generating the JNI sources.
 */
abstract class GenerateJniSourcesTask : DefaultTask() {

    @get:Inject
    protected abstract val fileOperations: FileSystemOperations

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Nested
    abstract val cProject: Property<CProject>

    @TaskAction
    fun generate() {
        val outputDirectory = fileOperations.clearAndGetAsFile(outputDirectory)
        val cProject = cProject.get()
        val packageName = cProject.packageName.get()
        val metadataFile = outputDirectory.resolve("$packageName/KsqliteJniGenerated.kt")

        metadataFile
            .apply { parentFile.mkdirs() }
            .writeText(createSqliteJniRuntimeMetadataContent(cProject))
    }
}