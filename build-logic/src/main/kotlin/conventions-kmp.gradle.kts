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