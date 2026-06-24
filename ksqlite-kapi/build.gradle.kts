plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    allTargets()

    sourceSets {
        commonMain.dependencies {
            api(projects.ksqliteTypes.core)
            implementation(projects.ksqliteCapi)
            implementation(libs.stately.concurrentCollections)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}