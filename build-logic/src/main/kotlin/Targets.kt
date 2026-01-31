import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/**
 * Adds Android JVM targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.androidJvmTargets(): List<KotlinMultiplatformAndroidLibraryTarget> {
    return listOf(extensions.getByName<KotlinMultiplatformAndroidLibraryTarget>("android").apply {
        val libs = project.libs
        namespace = project.projectNamespace

        compileSdk {
            version = release(libs.versions.android.sdk.compile.get().toInt())
        }

        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.target.default.get())
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
fun KotlinMultiplatformExtension.appleTargets(): List<KotlinNativeTarget> = buildList {
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
    add(watchosArm32())
    add(watchosDeviceArm64())
}

/**
 * Adds Jvm targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.jvmTargets(
    jvmTargetVersion: Provider<String>? = null
): List<KotlinJvmTarget> {
    return listOf(jvm {
        jvmTargetVersion?.get()?.let { version ->
            compilerOptions {
                jvmTarget = JvmTarget.fromTarget(version)
            }
        }
    })
}

/**
 * Adds Linux targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.linuxTargets(): List<KotlinNativeTarget> {
    return listOf(
        linuxX64(),
        linuxArm64()
    )
}

/**
 * Adds Windows targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.windowsTargets(): List<KotlinNativeTargetWithHostTests> {
    return listOf(mingwX64())
}

/**
 * Adds Js targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.jsTargets(): List<KotlinJsTargetDsl> {
    return listOf(js {
        useEsModules()
        browser()
    })
}

/**
 * Adds WasmJs targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.wasmJsTargets(
): List<KotlinWasmJsTargetDsl> {
    return listOf(wasmJs {
        browser()
    })
}

/**
 * Adds Web targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
fun KotlinMultiplatformExtension.webTargets() = buildList {
    addAll(jsTargets())
    addAll(wasmJsTargets())
}

/**
 * Adds Native targets to `this` [KotlinMultiplatformExtension] and returns them.
 */
@Suppress("DEPRECATION")
fun KotlinMultiplatformExtension.nativeTargets(
): List<KotlinNativeTarget> = buildList {
    addAll(androidNativeTargets())
    addAll(appleTargets())
    addAll(linuxTargets())
    addAll(windowsTargets())
}