import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
}

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
        //ndkPath = sqliteCompilerExtension.sqliteCompilationParameters.get().androidNdkToolchainPath

        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = libs.versions.cmake.get()
        }
    }
}