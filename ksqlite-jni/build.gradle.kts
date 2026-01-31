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
    packageName = projectNamespace,
    libraryName = "ksqlite-native",
    metadataFile = generatedSourceDirectory.map { it.file("$projectNamespace/KsqliteNativeJni.kt") }
)

val generateNativeContent by tasks.registering {
    dependsOn(generateSqliteCMakeListsTaskProvider)
    dependsOn(generateSqliteJniRuntimeMetadataTaskProvider)
}

registerTaskForIde(generateNativeContent) {
    // CMakeLists.txt file need to be generated or sync will fail so force task action(s) execution
    generateSqliteCMakeListsTaskProvider.get().let { generateTask ->
        generateTask.actions.forEach { it(generateTask) }
    }
}

kotlin {
    configureKotlin()

    target.compilations.configureEach {
        compileTaskProvider.configure {
            dependsOn(generateNativeContent)
        }
    }
}

android {
    namespace = projectNamespace

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.default.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.default.get())
    }

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

    sourceSets.named("main") {
        kotlin.directories += generatedSourceDirectory.get().asFile.absolutePath
    }
}