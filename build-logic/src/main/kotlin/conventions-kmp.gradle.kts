import com.android.build.api.withAndroid
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
@Suppress("UnstableApiUsage")
kotlin {
    configureKotlin()

    applyDefaultHierarchyTemplate {
        common {
            group("wal") {
                withAndroid()
                withJvm()
                group("native")
            }
        }
    }

    sourceSets.configureEach outer@{
        println("sourceSet = $name")
        when (name) {
            in SourceSetMainNatives -> languageSettings {
                optIn("kotlin.experimental.ExperimentalNativeApi")
                optIn("kotlinx.cinterop.BetaInteropApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }

            in SourceSetMainWebs -> languageSettings {
                optIn("kotlin.js.ExperimentalWasmJsInterop")
            }

            "androidDeviceTest" -> {

            }
        }
    }
}