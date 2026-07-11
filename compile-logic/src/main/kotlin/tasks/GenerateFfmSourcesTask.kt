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