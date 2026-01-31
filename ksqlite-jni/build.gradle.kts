import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import tasks.registerSqliteGenerateCMakeListsTask
import tasks.registerSqliteJniRuntimeMetadataTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
}

val generatedSourceDirectory: Provider<Directory> = layout.buildDirectory.map { directory ->
    directory.dir("generated/ksqlite/src/main/kotlin")
}

val generateSqliteCMakeListsTaskProvider = registerSqliteGenerateCMakeListsTask(
    cmakeListsFile = layout.buildDirectory.map { it.file("sqlite/CMakeLists.txt") },
    cmakeVersion = libs.versions.cmake.get()
)

val generateSqliteJniRuntimeMetadataTaskProvider = registerSqliteJniRuntimeMetadataTask(
    packageName = localNamespace,
    metadataFile = generatedSourceDirectory.map { it.file("$localNamespace/KsqliteJni.kt") }
)

kotlin {
    jvmToolchain(libs.versions.jvm.target.get().toInt())

    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.target.get())
    }
}

android {
    namespace = localNamespace

    compileSdk {
        version = release(libs.versions.android.sdk.compile.get().toInt())
    }

    defaultConfig {
        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }
    }

    externalNativeBuild {
        ndkVersion = libs.versions.android.ndk.get()
        ndkPath = ksqliteCompilerExtension.androidToolchain().get().path

        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = libs.versions.cmake.get()
        }
    }

    sourceSets.all {
        println("sourceSet -> ${this.name}, ${this.kotlin.name}")
    }
}

rootProject.tasks.named("prepareKotlinBuildScriptModel").configure {
    dependsOn(generateSqliteCMakeListsTaskProvider)
    dependsOn(generateSqliteJniRuntimeMetadataTaskProvider)
}