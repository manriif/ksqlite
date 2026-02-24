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

        commonMain.dependencies {
            implementation(projects.ksqlite.capi)
        }
    }
}

kotlinJsResConsumer {
    directory = ""
}

dependencies {
    jsConsumedResources(projects.ksqliteWeb)
    wasmConsumedResources(projects.ksqliteWeb)
}