package ksqlite.gradle

import ksqlite.gradle.wasm.extractWasmResources
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * Ksqlite Gradle Plugin.
 */
public class KsqlitePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions
            .create<KsqliteExtension>("ksqlite")
            .apply { configure() }

        val ksqliteVersion = this::class.java.classLoader
            .getResource("version.txt")
            ?.readText()
            ?.trim()
            ?: error("Failed to read Ksqlite version")

        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            target.extractWasmResources(extension.wasm, ksqliteVersion)
        }
    }
}