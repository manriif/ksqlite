@file:Suppress("HasPlatformType")

import tasks.registerSqliteCopyJniJavaSourceTask
import tasks.registerSqliteJniGenerateCMakeListsTask
import tasks.registerSqliteJniRuntimeMetadataTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
}

val generatedSourceDirectory = layout.buildDirectory.map { it.dir("generated/ksqlite/src/main") }
val generatedJavaSourceDirectory = generatedSourceDirectory.map { it.dir("java") }
val generatedKotlinSourceDirectory = generatedSourceDirectory.map { it.dir("kotlin") }
val ksqliteCmakeDirectory = layout.buildDirectory.map { it.dir("ksqlite") }

val generateSqliteJniCMakeListsTaskProvider = registerSqliteJniGenerateCMakeListsTask(
    cmakeListsFile = ksqliteCmakeDirectory.map { it.file("CMakeLists.txt") },
    cmakeVersion = libs.versions.cmake.get()
)

val generateSqliteJniRuntimeMetadataTaskProvider = registerSqliteJniRuntimeMetadataTask(
    packageName = projectNamespace,
    metadataFile = generatedKotlinSourceDirectory.map { directory ->
        directory.file("$projectNamespace/KsqliteJniGenerated.kt")
    }
)

val copySqliteJniJavaSourcesTaskProvider = registerSqliteCopyJniJavaSourceTask(
    sourcesDirectory = generatedJavaSourceDirectory
)

val generateSources by tasks.registering {
    dependsOn(generateSqliteJniCMakeListsTaskProvider)
    dependsOn(generateSqliteJniRuntimeMetadataTaskProvider)
    dependsOn(copySqliteJniJavaSourcesTaskProvider)
}

registerTaskForIde(generateSources) {
    // CMakeLists.txt file need to be generated or sync will fail so force task action(s) execution
    generateSqliteJniCMakeListsTaskProvider.get().let { generateTask ->
        generateTask.actions.forEach { it(generateTask) }
    }
}

kotlin {
    configureKotlin()

    target.compilations.configureEach {
        compileTaskProvider.configure {
            dependsOn(generateSources)
        }
    }
}

android {
    namespace = projectNamespace

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.android.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.android.get())
    }

    compileSdk {
        version = release(libs.versions.android.sdk.compile.get().toInt())
    }

    defaultConfig {
        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                arguments(
                    "-DKSQLITE_LIB_NAME=${ksqliteExtension.sqliteComponents.get().libraryName}",
                    "-DKSQLITE_CMAKE_DIR=${ksqliteCmakeDirectory.get().asFile.absolutePath}"
                )
            }
        }
    }

    externalNativeBuild {
        ndkVersion = libs.versions.android.ndk.get()

        ndkPath = ksqliteExtension.androidToolchain().get().path.takeIf { path ->
            file(path).exists()
        }

        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME) {
        java.directories += generatedJavaSourceDirectory.get().asFile.absolutePath
        kotlin.directories += generatedKotlinSourceDirectory.get().asFile.absolutePath
    }
}