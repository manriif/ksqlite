plugins {
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    androidJvmTargets()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.sqlite)
        }
    }
}