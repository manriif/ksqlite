import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

    var androidDeviceTest: KotlinSourceSet? = null
    var walTest: KotlinSourceSet? = null

    fun configureAndroidDeviceTest() {
        val android = androidDeviceTest
        val wal = walTest

        if (android != null && wal != null) {
            android.dependsOn(wal)
        }
    }

    sourceSets {
        configureEach outer@{
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
                    androidDeviceTest = this
                    dependsOn(getByName("commonTest"))
                    configureAndroidDeviceTest()

                    dependencies {
                        implementation(libs.androidx.testRunner)
                    }
                }

                "walTest" -> {
                    walTest = this
                    configureAndroidDeviceTest()
                }
            }
        }
    }
}