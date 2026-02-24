plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.opensavvy.resources.consumer)
    alias(libs.plugins.opensavvy.resources.producer)
}

kotlin {
    androidJvmTargets()
    jvmTargets()
    macosX64()
    macosArm64()
    //nativeTargets()
    webTargets()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }

        androidMain.dependencies {
            implementation(projects.sqliteJni)
        }

        jvmMain.dependencies {
            implementation(projects.sqliteFfm)
        }

        nativeMain.dependencies {
            implementation(projects.sqliteNative)
        }

        webMain.dependencies {
            implementation(libs.copyWebpackPlugin.get().run { devNpm(module.name, version!!) })
            implementation(projects.sqliteWeb)
        }
    }
}

kotlinJsResConsumer {
    directory = ""
}

dependencies {
    jsConsumedResources(projects.sqliteWeb)
    wasmConsumedResources(projects.sqliteWeb)
}