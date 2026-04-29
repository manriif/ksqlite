@file:Suppress("HasPlatformType")

import komple.platform.Platform
import komple.project.c.CLibraryType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val resourceNativeDirName = "native"
val ksqlitePackage = "ksqlite"
val generatedSourceDirectory = layout.buildDirectory.map { it.dir("generated/ksqlite/src/jvmMain") }
val generatedJavaSourceDirectory = generatedSourceDirectory.map { it.dir("java") }
val generatedKotlinSourceDirectory = generatedSourceDirectory.map { it.dir("kotlin") }

val libraries = Platform.run {
    listOf(macosArm64/*, macosX64, linuxX64, linuxArm64, mingwX64*/).map { platform ->
        komple.projects.kotlinSqlite.createLibrary(CLibraryType.Shared, platform)
    }
}

val generateSqliteFfmRuntimeMetadataTaskProvider = registerSqliteFfmRuntimeMetadataTask(
    packageName = ksqlitePackage,
    nativeDirectoryName = resourceNativeDirName,
    metadataFile = generatedKotlinSourceDirectory.map { directory ->
        directory.file("$projectNamespace/KsqliteFfmGenerated.kt")
    },
    platforms = provider { sqliteTargets.map { it.platform.get() } }
)

val javaBindings by komple.projects.kotlinSqlite.jextract.bindingGenerators.registering {
    options {
        headerClassName = cProject.libraryName
        includeFunctions = sqliteFunctions(true)
    }
}

val generateSources by tasks.registering(Copy::class) {
    dependsOn(generateSqliteFfmRuntimeMetadataTaskProvider)
    from(javaBindings.map { it.generateDirectory })
    into(generatedJavaSourceDirectory)
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
                libraries.forEach { library ->
                    from(library.libraryFile) {
                        into(resourceNativeDirName)
                    }
                }
            }
        }
    }
}