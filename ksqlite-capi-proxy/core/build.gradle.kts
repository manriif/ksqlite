plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    androidJvmTargets()
    jvmTargets()
    nativeTargets()
    webTargets()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutinesCore)
        }

        webMain.dependencies {
            api(libs.kotlin.wrappers.js)
        }
    }
}