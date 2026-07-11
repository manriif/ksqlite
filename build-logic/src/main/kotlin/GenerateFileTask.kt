import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Writes [content] to [outputFile].
 */
@CacheableTask
abstract class GenerateFileTask : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val content: Property<String>

    @TaskAction
    fun generate() {
        outputFile.get().asFile
            .apply { parentFile.mkdirs() }
            .writeText(content.get())
    }
}