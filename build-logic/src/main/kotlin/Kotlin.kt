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
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.HasProject

/**
 * Names of all native main sources sets.
 */
val SourceSetMainNatives = listOf(
    "nativeMain",
    "androidNativeMain",
    "androidNativeArm32Main",
    "androidNativeArm64Main",
    "androidNativeX86Main",
    "androidNativeX64Main",
    "appleMain",
    "macosMain",
    "macosX64Main",
    "macosArm64Main",
    "iosMain",
    "iosX64Main",
    "iosArm64Main",
    "iosSimulatorArm64Main",
    "tvosMain",
    "tvosX64Main",
    "tvosArm64Main",
    "tvosSimulatorArm64Main",
    "watchosMain",
    "watchosArm32Main",
    "watchosX64Main",
    "watchosArm64Main",
    "watchosDeviceArm64Main",
    "watchosSimulatorArm64Main",
    "linuxMain",
    "linuxX64Main",
    "linuxArm64Main",
    "mingwMain",
    "mingwX64Main",
)

/**
 * Names of all web main sources sets.
 */
val SourceSetMainWebs = listOf(
    "webMain",
    "jsMain",
    "wasmJsMain"
)

/**
 * Applies common Kotlin configuration.
 */
fun <Extension> Extension.configureKotlin()
        where Extension : KotlinBaseExtension,
              Extension : HasConfigurableKotlinCompilerOptions<*>,
              Extension : HasProject {
    explicitApi()

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_4
        apiVersion = KotlinVersion.KOTLIN_2_4
        allWarningsAsErrors = true
        progressiveMode = true

        freeCompilerArgs.run {
            add("-Xexpect-actual-classes")
            add("-Xreturn-value-checker=full")
            add("-Xcontext-sensitive-resolution")
        }
    }

    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(project.libs.versions.jvm.toolchain.min.get())
    }
}

/**
 * Sets the jvm toolchain's java language version to a value that supports FFM.
 */

fun <Extension> Extension.jvmToolchainFfm()
        where Extension : KotlinBaseExtension,
              Extension : HasProject {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(project.libs.versions.jvm.toolchain.ffm.get())
    }
}