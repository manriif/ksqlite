import com.android.build.api.dsl.AndroidLibrarySourceSet
import modules.cmakeArguments
import modules.createSqliteJniRuntimeMetadataContent

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
    alias(kompleLibs.plugins.komple)
}

val generatedKotlinSourceDirectory = layout.buildDirectory.dir("generated/ksqlite/src/main/kotlin")

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

registerTaskForIde(generateJniMetadata)

kotlin {
    configureKotlin()

    target.compilations.configureEach {
        compileTaskProvider.configure {
            dependsOn(generateJniMetadata)
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
                arguments += komple.projects.kotlinSqlite.kProject.cmakeArguments()
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
        kotlin.directories += generatedKotlinSourceDirectory.get().asFile.absolutePath
    })
}