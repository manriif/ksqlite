/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/**
 * Adds Android JVM targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.androidJvmTargets(): List<KotlinMultiplatformAndroidLibraryTarget> {
    val libs = project.libs

    val android = extensions.getByName<KotlinMultiplatformAndroidLibraryTarget>("android").apply {
        namespace = project.localNamespace

        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.toolchain.min.get())
        }

        compileSdk {
            version = release(libs.versions.android.sdk.compile.get().toInt())
        }

        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "HOST"

            targetSdk {
                version = release(libs.versions.android.sdk.compile.get().toInt())
            }

            @Suppress("UnstableApiUsage")
            managedDevices {
                localDevices {
                    create("pixel2api30") {
                        device = "Pixel 2"
                        apiLevel = 30
                        systemImageSource = "aosp"
                    }
                }
            }
        }
    }

    val androidDeviceTest = sourceSets.named("androidDeviceTest").apply {
        // Well this is added here to keep other build files look cleaner
        configure {
            dependencies {
                implementation(libs.androidx.testRunner)
            }
        }
    }

    // FIXME => w: Invalid Source Set Dependency Across Trees
    //  Well, instrumented tests on Android are only required to get the ksqlite-jni lib being
    //  loaded. There is no dependency on an Android specific API, apart from the native log API
    //  which is itself unnecessary.
    //  Until the warning upgrades to an error, or an acceptable workaround other than duplicating
    //  the whole tests is found, we still use it as testing should not be that complicated
    sourceSets.whenObjectAdded {
        if (name == "nonWebTest") {
            androidDeviceTest.get().dependsOn(this)
        }
    }

    return listOf(android)
}

/**
 * Adds Android Native targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.androidNativeTargets(): List<KotlinNativeTarget> = listOf(
    androidNativeArm32(),
    androidNativeArm64(),
    androidNativeX86(),
    androidNativeX64()
)

/**
 * Adds Apple targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@Suppress("DEPRECATION")
fun KotlinMultiplatformExtension.appleTargets(): List<KotlinNativeTarget> = buildList {
    add(macosX64())
    add(macosArm64())

    add(iosX64())
    add(iosArm64())
    add(iosSimulatorArm64())

    add(tvosX64())
    add(tvosArm64())
    add(tvosSimulatorArm64())

    add(watchosX64())
    add(watchosArm64())
    add(watchosSimulatorArm64())
    add(watchosArm32())
    add(watchosDeviceArm64())
}

/**
 * Adds Jvm targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.jvmTargets(): List<KotlinJvmTarget> = listOf(jvm {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(project.libs.versions.jvm.toolchain.ffm.get())
    }
})

/**
 * Adds Linux targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.linuxTargets(): List<KotlinNativeTarget> = listOf(
    linuxX64(),
    linuxArm64()
)

/**
 * Adds Windows targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.windowsTargets(): List<KotlinNativeTargetWithHostTests> {
    return listOf(mingwX64())
}

/**
 * Adds Js targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.jsTargets(): List<KotlinJsTargetDsl> = listOf(js {
    useEsModules()
    browser()

    compilerOptions {
        freeCompilerArgs.add("-Xes-long-as-bigint")
    }
})

/**
 * Adds WasmJs targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.wasmJsTargets(): List<KotlinWasmJsTargetDsl> = listOf(wasmJs {
    browser()
})

/**
 * Adds Web targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.webTargets() = buildList {
    addAll(jsTargets())
    addAll(wasmJsTargets())
}

/**
 * Adds Native targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.nativeTargets(): List<KotlinNativeTarget> = buildList {
    addAll(androidNativeTargets())
    addAll(appleTargets())
    addAll(linuxTargets())
    addAll(windowsTargets())
}

/**
 * Adds all supported targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.allTargets(): List<KotlinTarget> = buildList {
    addAll(androidJvmTargets())
    addAll(jvmTargets())
    addAll(webTargets())
    addAll(nativeTargets())
}
