plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

kotlin {
    configureWasmResources(projects.ksqliteWasmResources)
    allTargets()

    sourceSets {
        commonMain.dependencies {
            api(projects.ksqliteTypes.ksqliteTypesCore)
            implementation(projects.ksqliteTypes.ksqliteTypesInternal)
            implementation(libs.stately.concurrentCollections)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // This is currently required due to a suspicious issue in Kotlin/JS
            implementation(libs.kotlinx.coroutinesTest)
        }

        androidMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignJni)
        }

        jvmMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignFfm)
        }

        nativeMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignCinterop)
        }

        webMain.dependencies {
            implementation(projects.ksqliteForeign.ksqliteForeignWasm)
        }
    }
}