package tasks

import komple.task.clearAndGetAsFile
import modules.createSqliteJniRuntimeMetadataContent
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Task responsible for generating the JNI sources.
 */
@DisableCachingByDefault
abstract class GenerateJniSourcesTask : GenerateSourcesTask() {

    @TaskAction
    fun generate() {
        val outputDirectory = fileOperations.clearAndGetAsFile(outputDirectory)
        val packageName = packageName.get()

        val content = createSqliteJniRuntimeMetadataContent(
            packageName = packageName,
            libraryName = libraryName.get()
        )

        outputDirectory
            .resolve("$packageName/KsqliteJniGenerated.kt")
            .write(content)
    }
}