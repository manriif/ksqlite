@file:Suppress("HasPlatformType")

import compilation.SqliteTarget
import compilation.sharedLibraryFileName
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
val sqlitePackage = "sqlite"
val generatedSourceDirectory = layout.buildDirectory.map { it.dir("generated/ksqlite/src/jvmMain") }
val generatedJavaSourceDirectory = generatedSourceDirectory.map { it.dir("java") }
val generatedKotlinSourceDirectory = generatedSourceDirectory.map { it.dir("kotlin") }
val libsDir = layout.buildDirectory.map { it.dir("sqlite/$resourceNativeDirName") }

fun createSqliteTarget(
    operatingSystem: OperatingSystem,
    architecture: Architecture,
): SqliteTarget = objects.newInstance<SqliteTarget>().apply {
    val platform = Platform(operatingSystem, architecture)
    this.platform = platform

    this.libraryFile = libsDir.zip(ksqliteExtension.compilationParams) { dir, params ->
        dir.file(platform.operatingSystem.library.sharedLibraryFileName(params.libraryName))
    }
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
    packageName = sqlitePackage,
    outputDirectory = generatedJavaSourceDirectory
)

val sqliteCompileSharedTaskProvider = registerSqliteCompileSharedTask("Jvm") {
    dependsOn(jextractInstallTaskProvider)
    // TODO zig dependency
    outputs.dir(libsDir)
    targets = sqliteTargets
}

val generateSqliteFfmRuntimeMetadataTaskProvider = registerSqliteFfmRuntimeMetadataTask(
    packageName = sqlitePackage,
    nativeDirectoryName = resourceNativeDirName,
    metadataFile = generatedKotlinSourceDirectory.map { directory ->
        directory.file("$projectNamespace/KsqliteFfm.kt")
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
                dependsOn(sqliteCompileSharedTaskProvider)
                inputs.dir(libsDir)

                from(libsDir) {
                    into(resourceNativeDirName)
                }
            }
        }
    }
}