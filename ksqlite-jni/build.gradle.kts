import com.android.build.api.dsl.AndroidLibrarySourceSet
import modules.copyJniJavaSources
import modules.createSqliteCMakeListsContent
import modules.createSqliteJniRuntimeMetadataContent
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
    alias(kompleLibs.plugins.komple)
}

val generatedSourceDirectory = layout.buildDirectory.dir("generated/ksqlite/src/main")
val generatedJavaSourceDirectory = generatedSourceDirectory.map { it.dir("java") }
val generatedKotlinSourceDirectory = generatedSourceDirectory.map { it.dir("kotlin") }
val ksqliteCmakeDirectory = layout.buildDirectory.dir("ksqlite")

val generateCmakeLists by tasks.registeringKsqlite {
    val cProject = komple.projects.kotlinSqlite.kProject
    val sqliteDirectory = ksqlite.sqliteDirectory
    val cmakeListsFile = ksqliteCmakeDirectory.map { it.file("CMakeLists.txt") }
    val cmakeVersion = libs.versions.cmake

    inputs.dir(sqliteDirectory)
    outputs.file(cmakeListsFile)

    doLast {
        cmakeListsFile.writeContent(
            createSqliteCMakeListsContent(
                cProject = cProject,
                cmakeVersion = cmakeVersion.get(),
                sqliteDirectory = sqliteDirectory.get().asFile
            )
        )
    }
}

val generateJniMetadata by tasks.registeringKsqlite {
    val cProject = komple.projects.kotlinSqlite.kProject

    val metadataFile = generatedKotlinSourceDirectory.zip(cProject.packageName) { directory, name ->
        directory.file("$name/KsqliteJniGenerated.kt")
    }

    outputs.file(metadataFile)

    doLast {
        metadataFile.writeContent(createSqliteJniRuntimeMetadataContent(cProject))
    }
}

val copyJniJavaSources by tasks.registeringKsqlite {
    val fileOperations = serviceOf<FileSystemOperations>()
    val sqliteDirectory = ksqlite.sqliteDirectory
    val outputDirectory = generatedJavaSourceDirectory

    inputs.dir(sqliteDirectory)
    outputs.dir(outputDirectory)

    doLast {
        copyJniJavaSources(
            fileOperations = fileOperations,
            sqliteDirectory = sqliteDirectory.get().asFile,
            outputDirectory = fileOperations.clearAndGetFile(outputDirectory)
        )
    }
}

val generateJniSources by tasks.registeringKsqlite {
    dependsOn(generateJniMetadata)
    dependsOn(copyJniJavaSources)
}

val generateCmakeListWithJniSource by tasks.registeringKsqlite {
    dependsOn(generateCmakeLists)
    dependsOn(generateJniSources)
}

registerTaskForIde(generateCmakeListWithJniSource) {
    // CMakeLists.txt file need to be generated or sync will fail so force task action(s) execution
    generateCmakeListWithJniSource.get().let { generateTask ->
        generateTask.actions.forEach { it(generateTask) }
    }
}

kotlin {
    configureKotlin()

    target.compilations.configureEach {
        compileTaskProvider.configure {
            dependsOn(generateJniSources)
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
                    "-DKSQLITE_LIB_NAME=${ksqlite.libraryName.get()}",
                    "-DKSQLITE_CMAKE_DIR=${ksqliteCmakeDirectory.get().asFile.absolutePath}"
                )
            }
        }
    }

    externalNativeBuild {
        ndkVersion = libs.versions.android.ndk.get()

        cmake {
            version = libs.versions.cmake.get()
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    // FIXME AGP 9.2.0 broken cast com.android.build.gradle.api.AndroidLibrarySourceSet =>
    //  com.android.build.api.dsl.AndroidLibrarySourceSet
    sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME, Action<AndroidLibrarySourceSet> {
        java.directories += generatedJavaSourceDirectory.get().asFile.absolutePath
        kotlin.directories += generatedKotlinSourceDirectory.get().asFile.absolutePath
    })
}