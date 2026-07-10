import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import modules.cmakeArguments
import tasks.GenerateJniSourcesTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
    alias(kompleLibs.plugins.komple)
}

val generateJniSources by tasks.registeringKsqlite<GenerateJniSourcesTask> {
    outputDirectory = layout.buildDirectory.dir("generated/ksqlite/src/main/kotlin")
    cProject = komple.projects.kotlinSqlite.kProject
}

registerTaskForIde(generateJniSources)

kotlin {
    configureKotlin()
}

android {
    namespace = localNamespace

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

    androidComponents {
        onVariants { variant ->
            variant.sources.kotlin?.addGeneratedSourceDirectory(
                taskProvider = generateJniSources,
                wiredWith = GenerateJniSourcesTask::outputDirectory
            )
        }
    }
}

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            javadocJar = JavadocJar.Empty(),
            sourcesJar = SourcesJar.Sources()
        )
    )
}