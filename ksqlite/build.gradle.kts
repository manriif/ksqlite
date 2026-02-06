plugins {
    //alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    //androidJvmTargets()
    //jvmTargets()
    //macosX64()
    //nativeTargets()
    webTargets()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        /*androidMain.dependencies {
            implementation(projects.ksqliteJni)
        }

        jvmMain.dependencies {
            implementation(projects.ksqliteFfm)
        }

        nativeMain.dependencies {
            implementation(projects.ksqliteNative)
        }*/

        webMain.dependencies {
            implementation(projects.ksqliteWeb)
        }
    }
}