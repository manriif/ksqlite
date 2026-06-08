plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    androidJvmTargets()
    jvmTargets()
    nativeTargets()
    webTargets()

    sourceSets.commonMain.dependencies {
        api(projects.ksqliteCapiProxy.core)
    }
}