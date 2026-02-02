@file:Suppress("HasPlatformType")

import compilation.SqliteTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import platform.Architecture
import platform.OperatingSystem
import platform.Platform
import tasks.registerJextractGenerateBindingsTask
import tasks.registerSqliteCompileSharedTask
import tasks.registerSqliteFfmRuntimeMetadataTask

plugins {
    alias(libs.plugins.conventions.kmp)
}

val resourceNativeDirName = "native"
val generatedSourceDirectory = layout.buildDirectory.map { it.dir("generated/ksqlite/src/jvmMain") }
val generatedJavaSourceDirectory = generatedSourceDirectory.map { it.dir("java") }
val generatedKotlinSourceDirectory = generatedSourceDirectory.map { it.dir("kotlin") }
val librariesDirectory = layout.buildDirectory.map { it.dir("sqlite/$resourceNativeDirName") }

fun createSqliteTarget(
    operatingSystem: OperatingSystem,
    architecture: Architecture,
): SqliteTarget = objects.newInstance<SqliteTarget>().apply {
    val platform = Platform(operatingSystem, architecture)
    this.platform.set(platform)
    this.libraryDirectory.set(librariesDirectory.map { it.dir(platform.name) })
}

val sqliteTargets = listOf(
    //createSqliteTarget(OperatingSystem.Linux, Architecture.Arm64),
    //createSqliteTarget(OperatingSystem.Linux, Architecture.X64),
    //createSqliteTarget(OperatingSystem.MacOS, Architecture.Arm64),
    createSqliteTarget(OperatingSystem.MacOS, Architecture.X64),
    //createSqliteTarget(OperatingSystem.Windows, Architecture.Arm64),
    //createSqliteTarget(OperatingSystem.Windows, Architecture.X64),
)

val generateBindingsTaskProvider = registerJextractGenerateBindingsTask(
    packageName = projectNamespace,
    outputDirectory = generatedJavaSourceDirectory
)

val compileSharedTaskProvider = registerSqliteCompileSharedTask().apply {
    configure {
        outputs.dir(librariesDirectory)
        targets = sqliteTargets
    }
}

val generateSqliteFfmRuntimeMetadataTaskProvider = registerSqliteFfmRuntimeMetadataTask(
    packageName = projectNamespace,
    nativeDirectoryName = resourceNativeDirName,
    metadataFile = generatedKotlinSourceDirectory.map { directory ->
        directory.file("$projectNamespace/KsqliteNativeFfm.kt")
    },
    platforms = provider { sqliteTargets.map { it.platform.get() } }
)

val generateSources by tasks.registering {
    dependsOn(generateBindingsTaskProvider)
    dependsOn(generateSqliteFfmRuntimeMetadataTaskProvider)
}

registerTaskForIde(generateSources)

kotlin {
    jvmTargets().forEach { target ->
        target.configureJvmTarget()
    }

    sourceSets.jvmMain {
        kotlin.srcDir(generatedJavaSourceDirectory)
        kotlin.srcDir(generatedKotlinSourceDirectory)
    }
}

fun KotlinJvmTarget.configureJvmTarget() {
    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        compileTaskProvider.configure {
            dependsOn(generateSources)
        }

        checkNotNull(compileJavaTaskProvider).configure {
            dependsOn(generateSources)
            source(generatedJavaSourceDirectory)
        }

        tasks.named<ProcessResources>(processResourcesTaskName).apply {
            configure {
                dependsOn(compileSharedTaskProvider)
                inputs.dir(librariesDirectory)

                from(librariesDirectory) {
                    into(resourceNativeDirName)
                }
            }
        }
    }
}