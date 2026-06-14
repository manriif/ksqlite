plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

val extractWasmResources = registerExtractWasmResourcesTask(projects.ksqliteForeign.wasm)

kotlin {
    androidJvmTargets()
    jvmTargets()
    nativeTargets()
    webTargets()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.stately.concurrentCollections)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
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
            resources.srcDir(extractWasmResources)

            dependencies {
                implementation(libs.copyWebpackPlugin.get().run { devNpm(module.name, version!!) })
            }
        }
    }
}