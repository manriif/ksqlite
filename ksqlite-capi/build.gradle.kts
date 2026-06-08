plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

val extractWasmResources = registerExtractWasmResourcesTask(projects.ksqliteWasm)

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
            implementation(projects.ksqliteJni)
        }

        jvmMain.dependencies {
            implementation(projects.ksqliteFfm)
        }

        nativeMain.dependencies {
            implementation(projects.ksqliteCinterop)
        }

        webMain.dependencies {
            implementation(projects.ksqliteWasm)
        }

        webTest {
            resources.srcDir(extractWasmResources)

            dependencies {
                implementation(libs.copyWebpackPlugin.get().run { devNpm(module.name, version!!) })
            }
        }
    }
}