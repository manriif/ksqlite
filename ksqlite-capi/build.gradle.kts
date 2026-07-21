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
plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    jvmToolchainFfm()
    configureWasmResources(projects.ksqliteWasmResources)
    allTargets()

    sourceSets {
        commonMain.dependencies {
            api(projects.ksqliteTypes.ksqliteTypesCore)
            implementation(projects.ksqliteTypes.ksqliteTypesInternal)
            implementation(libs.stately.concurrentCollections)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignJni)
        }

        jvmMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignFfm)
        }

        nativeMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignCinterop)
        }

        webMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignWasm)
        }
    }
}