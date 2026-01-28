plugins {
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    androidJvmTargets()
    /*jvmTargets()
    nativeTargets()
    webTargets()*/

    sourceSets {
        commonMain.dependencies {
            implementation(projects.ksqlite)
        }
    }
}