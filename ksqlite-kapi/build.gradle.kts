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
            implementation(projects.ksqliteCapi)
            implementation(libs.stately.concurrentCollections) // TODO regular concurrency
            api(libs.kotlinx.coroutinesCore)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}