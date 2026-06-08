import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.HasProject

/**
 * Names of all native main sources sets.
 */
val SourceSetMainNatives = listOf(
    "nativeMain",
    "androidNativeMain",
    "androidNativeArm32Main",
    "androidNativeArm64Main",
    "androidNativeX86Main",
    "androidNativeX64Main",
    "appleMain",
    "macosMain",
    "macosX64Main",
    "macosArm64Main",
    "iosMain",
    "iosX64Main",
    "iosArm64Main",
    "iosSimulatorArm64Main",
    "tvosMain",
    "tvosX64Main",
    "tvosArm64Main",
    "tvosSimulatorArm64Main",
    "watchosMain",
    "watchosArm32Main",
    "watchosX64Main",
    "watchosArm64Main",
    "watchosDeviceArm64Main",
    "watchosSimulatorArm64Main",
    "linuxMain",
    "linuxX64Main",
    "linuxArm64Main",
    "mingwMain",
    "mingwX64Main",
)

/**
 * Names of all web main sources sets.
 */
val SourceSetMainWebs = listOf(
    "webMain",
    "jsMain",
    "wasmJsMain"
)

/**
 * Applies common Kotlin configuration.
 */
fun <Extension> Extension.configureKotlin()
        where Extension : KotlinBaseExtension,
              Extension : HasConfigurableKotlinCompilerOptions<*>,
              Extension : HasProject {
    explicitApi()

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_4
        apiVersion = KotlinVersion.KOTLIN_2_4
        //allWarningsAsErrors = true
        progressiveMode = true

        freeCompilerArgs.run {
            add("-Xcontext-parameters")
            add("-Xexpect-actual-classes")
            add("-Xexplicit-backing-fields")
            add("-Xreturn-value-checker=full")
            add("-Xcontext-sensitive-resolution")
        }
    }

    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(project.libs.versions.jvm.toolchain.get())
    }
}