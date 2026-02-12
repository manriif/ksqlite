plugins {
    //alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.opensavvy.resources.consumer)
    alias(libs.plugins.opensavvy.resources.producer)
}

kotlin {
    //androidJvmTargets()
    jvmTargets()
    macosX64()
    //nativeTargets()
    webTargets()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }

        /*androidMain.dependencies {
            implementation(projects.ksqliteJni)
        }*/

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
    }
}

kotlinJsResConsumer {
    directory = ""
}

dependencies {
    jsConsumedResources(projects.ksqliteWeb)
    wasmConsumedResources(projects.ksqliteWeb)
}