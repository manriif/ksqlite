plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

val extractWasmResources = registerExtractWasmResourcesTask(projects.ksqliteWeb)

kotlin {
    androidJvmTargets()
    jvmTargets()
    macosArm64()
    //nativeTargets()
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
            implementation(projects.ksqliteNative)
        }

        webMain.dependencies {
            implementation(libs.copyWebpackPlugin.get().run { devNpm(module.name, version!!) })
            implementation(projects.ksqliteWeb)
        }

        webTest {
            resources.srcDir(extractWasmResources)
        }
    }
}