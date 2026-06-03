plugins {
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.conventions.kmp)
}

val wasmResourcesDirectory = layout.buildDirectory.dir("generated/ksqlite/src/webTest/resources")

@Suppress("UnstableApiUsage")
val wasmResources by configurations.resolvable(WASM_RESOURCES_CONFIG_NAME_CONSUMER)  {
    applyWasmResourcesAttributes(this)
}

val extractWasmResources by tasks.registering(Sync::class) {
    dependsOn(wasmResources.buildDependencies)
    from(wasmResources.map(::zipTree))
    into(wasmResourcesDirectory)
}

dependencies {
    project(
        mapOf(
            "path" to projects.ksqliteWeb.path,
            "configuration" to WASM_RESOURCES_CONFIG_NAME_PRODUCER
        )
    )
}

kotlin {
    androidJvmTargets()
    jvmTargets()
    macosArm64()
    //nativeTargets()
    webTargets()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.stately.concurrentCollections)
        }

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

        webMain.dependencies {
            implementation(libs.copyWebpackPlugin.get().run { devNpm(module.name, version!!) })
            implementation(projects.ksqliteWeb)
        }

        webTest {
            resources.srcDir(extractWasmResources)
        }
    }
}