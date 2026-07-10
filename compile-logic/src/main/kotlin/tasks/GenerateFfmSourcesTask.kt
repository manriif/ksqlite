package tasks

import komple.project.c.CCompilation
import komple.project.c.CProject
import komple.task.clearAndGetAsFile
import modules.createKsqliteFfmRuntimeMetadataContent
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Task responsible for generating the FFM sources.
 */
abstract class GenerateFfmSourcesTask : DefaultTask() {

    @get:Inject
    protected abstract val fileOperations: FileSystemOperations

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    abstract val compilations: ListProperty<CCompilation>

    @get:Nested
    abstract val cProject: Property<CProject>

    @TaskAction
    fun generate() {
        val outputDirectory = fileOperations.clearAndGetAsFile(outputDirectory)
        val cProject = cProject.get()
        val compilations = compilations.get()
        val packageName = cProject.packageName.get()
        val metadataFile = outputDirectory.resolve("$packageName/KsqliteFfmGenerated.kt")

        metadataFile
            .apply { parentFile.mkdirs() }
            .writeText(createKsqliteFfmRuntimeMetadataContent(cProject, compilations))
    }
}