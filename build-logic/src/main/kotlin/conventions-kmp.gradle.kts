import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

    var androidDeviceTest: KotlinSourceSet? = null
    var nonWeb: KotlinSourceSet? = null

    fun configureAndroidDeviceTest() {
        val androidDevTest = androidDeviceTest
        val nonWeb = nonWeb

        if (androidDevTest != null && nonWeb != null) {
            androidDevTest.dependsOn(nonWeb)
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

                "nonWebTest" -> {
                    nonWeb = this
                    configureAndroidDeviceTest()
                }
            }
        }
    }
}