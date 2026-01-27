import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmWasiTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/**
 * Adds all supported targets to `this` [KotlinMultiplatformExtension].
 */
fun KotlinMultiplatformExtension.allTargets(
    androidNative: Boolean = true,
    linuxArm: Boolean = true,
    webBrowser: Boolean = true,
    webNode: Boolean = true,
    wasmWasi: Boolean = true,
    watchosArm32: Boolean = true,
    watchosDeviceArm64: Boolean = true,
) {
    androidJvmTargets()
    jvmTargets()

    nativeTargets(
        androidNative = androidNative,
        linuxArm = linuxArm,
        watchosArm32 = watchosArm32,
        watchosDeviceArm64 = watchosDeviceArm64,
    )

    webTargets(
        browser = webBrowser,
        node = webNode
    )

    if (wasmWasi) {
        wasmWasiTargets()
    }
}

/**
 * Adds Native targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@Suppress("DEPRECATION")
fun KotlinMultiplatformExtension.nativeTargets(
    androidNative: Boolean = true,
    linuxArm: Boolean = true,
    watchosArm32: Boolean = true,
    watchosDeviceArm64: Boolean = true
): List<KotlinNativeTarget> = buildList {
    if (androidNative) {
        addAll(androidNativeTargets())
    }

    addAll(appleTargets(
        watchosArm32 = watchosArm32,
        watchosDeviceArm64 = watchosDeviceArm64
    ))

    addAll(linuxTargets(arm = linuxArm))
    addAll(windowsTargets())
}

/**
 * Adds Web targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.webTargets(
    browser: Boolean = true,
    node: Boolean = true
) {
    jsTargets(browser = browser, node = node)
    wasmJsTargets(browser = browser, node = node)
}

/**
 * Adds Android JVM targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.androidJvmTargets(): List<KotlinMultiplatformAndroidLibraryTarget> {
    return listOf(extensions.getByName<KotlinMultiplatformAndroidLibraryTarget>("android").apply {
        val libs = project.libs
        namespace = project.localNamespace

        compileSdk {
            version = release(libs.versions.android.sdk.compile.get().toInt())
        }

        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.target.get())
                }
            }
        }
    })
}

/**
 * Adds Android Native targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.androidNativeTargets(): List<KotlinNativeTarget> {
    return listOf(
        androidNativeArm32(),
        androidNativeArm64(),
        androidNativeX86(),
        androidNativeX64()
    )
}

/**
 * Adds Apple targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@Suppress("DEPRECATION")
fun KotlinMultiplatformExtension.appleTargets(
    watchosArm32: Boolean = true,
    watchosDeviceArm64: Boolean = true
): List<KotlinNativeTarget> = buildList {
    add(macosX64())
    add(macosArm64())

    add(iosX64())
    add(iosArm64())
    add(iosSimulatorArm64())

    add(tvosX64())
    add(tvosArm64())
    add(tvosSimulatorArm64())

    add(watchosX64())
    add(watchosArm64())
    add(watchosSimulatorArm64())

    if (watchosArm32) {
        add(watchosArm32())
    }

    if (watchosDeviceArm64) {
        add(watchosDeviceArm64())
    }
}

/**
 * Adds Js targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.jsTargets(
    browser: Boolean = true,
    node: Boolean = true
): List<KotlinJsTargetDsl> {
    return listOf(js {
        useEsModules()
        configureCommonWebTarget(browser = browser, node = node)
    })
}

/**
 * Adds Jvm targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.jvmTargets(): List<KotlinJvmTarget> {
    return listOf(jvm())
}

/**
 * Adds Linux targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.linuxTargets(
    arm: Boolean = true
): List<KotlinNativeTarget> = buildList {
    add(linuxX64())

    if (arm) {
        add(linuxArm64())
    }
}

/**
 * Adds WasmJs targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.wasmJsTargets(
    browser: Boolean = true,
    node: Boolean = true
): List<KotlinWasmJsTargetDsl> {
    return listOf(wasmJs {
        configureCommonWebTarget(browser = browser, node = node)
    })
}

/**
 * Adds WasmWasi targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.wasmWasiTargets(): List<KotlinWasmWasiTargetDsl> {
    return listOf(wasmWasi {
        nodejs()
    })
}

/**
 * Adds Windows targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.windowsTargets(): List<KotlinNativeTargetWithHostTests> {
    return listOf(mingwX64())
}

/**
 * Configures [this] JS target.
 */
private fun KotlinJsTargetDsl.configureCommonWebTarget(browser: Boolean, node: Boolean) {
    if (browser) {
        browser()
    }

    if (node) {
        nodejs()
    }
}

///////////////////////////////////////////////////////////////////////////
// Hierarchy
///////////////////////////////////////////////////////////////////////////

/**
 * Only includes new Android Library target in `this` group.
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun KotlinHierarchyBuilder.withAndroidJvm() {
    withCompilations { it.target is KotlinMultiplatformAndroidLibraryTarget }
}