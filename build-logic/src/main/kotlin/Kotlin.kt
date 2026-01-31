import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.HasProject

/**
 * Applies common Kotlin configuration.
 */
fun <Extension> Extension.configureKotlin(
    jvmVersion: Provider<String> = project.libs.versions.jvm.target.default
) where Extension : KotlinBaseExtension,
        Extension : HasConfigurableKotlinCompilerOptions<*>,
        Extension : HasProject {
    explicitApi()

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_3
        apiVersion = KotlinVersion.KOTLIN_2_3
        allWarningsAsErrors = true
        progressiveMode = true

        freeCompilerArgs.run {
            add("-Xreturn-value-checker=full")
            add("-Xexpect-actual-classes")
            add("-Xcontext-parameters")
            add("-Xexplicit-backing-fields")
        }
    }

    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion.get())
    }
}