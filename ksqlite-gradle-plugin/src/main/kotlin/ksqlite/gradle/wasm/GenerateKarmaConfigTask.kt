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
                    prefix: "base/kotlin/ksqlite"
                }
            """.trimIndent()
        )
    }
}