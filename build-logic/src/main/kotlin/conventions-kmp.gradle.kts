import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

plugins {
    org.jetbrains.kotlin.multiplatform
    id("conventions-common")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    configureKotlin()

    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                withJvm()
                group("native")
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