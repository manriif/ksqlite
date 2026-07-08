plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    configureWasmResources(projects.ksqliteForeign.wasm)
    allTargets()

    sourceSets {
        commonMain.dependencies {
            api(projects.ksqliteTypes.core)
            implementation(projects.ksqliteTypes.internal)
            implementation(libs.stately.concurrentCollections)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // This is currently required due to a suspicious issue in Kotlin/JS
            implementation(libs.kotlinx.coroutinesTest)
        }

        androidMain.dependencies {
            implementation(projects.ksqliteForeign.jni)
        }

        jvmMain.dependencies {
            implementation(projects.ksqliteForeign.ffm)
        }

        nativeMain.dependencies {
            implementation(projects.ksqliteForeign.cinterop)
        }

        webMain.dependencies {
            implementation(projects.ksqliteForeign.wasm)
        }

        webTest {
            dependencies {
                implementation(libs.copyWebpackPlugin.get().run { devNpm(module.name, version!!) })
            }
        }
    }
}