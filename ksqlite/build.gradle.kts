plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    androidJvmTargets()
    jvmTargets()

    /*nativeTargets()
    webTargets()*/

    sourceSets {
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
    }
}