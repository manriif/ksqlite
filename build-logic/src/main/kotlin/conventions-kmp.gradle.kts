import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    org.jetbrains.kotlin.multiplatform
    id("conventions-common")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
@Suppress("UnstableApiUsage")
kotlin {
    configureKotlin()

    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                withAndroid()
                withJvm()
                group("native")
            }

            group("nonAndroid") {
                withJvm()
                group("native")
                group("web")
            }
        }
    }

    sourceSets.configureEach {
        when (name) {
            in SourceSetMainNatives -> languageSettings {
                optIn("kotlin.experimental.ExperimentalNativeApi")
                optIn("kotlinx.cinterop.BetaInteropApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }

            in SourceSetMainWebs -> languageSettings {
                optIn("kotlin.js.ExperimentalWasmJsInterop")
            }
        }
    }
}