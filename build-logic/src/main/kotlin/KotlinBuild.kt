import org.gradle.api.provider.ListProperty
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder

private const val KOMOGEN_REPOSITORY_OPTIN_PACKAGE = "komogen.repository.optin"

/**
 * Configures language settings adding common opt-ins.
 */
fun KotlinMultiplatformExtension.languageSettings(
    komogenExperimentalApi: Boolean = false,
    komogenRepositoryGenerator: Boolean = false,
    komogenRepositoryNamespace: Boolean = false,
    komogenNamespace: Boolean = false,
    komogenUnstableApi: Boolean = false,
    experimentalAtomicApi: Boolean = false,
    experimentalWasmJsInterop: Boolean = false,
    custom: (LanguageSettingsBuilder.() -> Unit)? = null
) {
    sourceSets.run {
        all {
            with(languageSettings) {
                if (komogenExperimentalApi) {
                    optIn("$KOMOGEN_REPOSITORY_OPTIN_PACKAGE.KomogenExperimentalApi")
                }

                if (komogenRepositoryGenerator) {
                    optIn("$KOMOGEN_REPOSITORY_OPTIN_PACKAGE.KomogenRepositoryGenerator")
                }

                if (komogenRepositoryNamespace) {
                    optIn("$KOMOGEN_REPOSITORY_OPTIN_PACKAGE.KomogenRepositoryNamespace")
                }

                if (komogenNamespace) {
                    optIn("$KOMOGEN_REPOSITORY_OPTIN_PACKAGE.KomogenNamespace")
                }

                if (komogenUnstableApi) {
                    optIn("$KOMOGEN_REPOSITORY_OPTIN_PACKAGE.KomogenUnstableApi")
                }

                if (experimentalAtomicApi) {
                    optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
                }

                custom?.invoke(this)
            }
        }

        if (experimentalWasmJsInterop) {
            listOf(jsMain, wasmJsMain, webMain).forEach { sourceSet ->
                sourceSet.configure {
                    languageSettings {
                        optIn("kotlin.js.ExperimentalWasmJsInterop")
                    }
                }
            }
        }
    }
}

/**
 * Adds compiler arguments.
 */
fun HasConfigurableKotlinCompilerOptions<*>.compilerArgs(
    expectActualClasses: Boolean = false,
    contextParameters: Boolean = false,
    explicitBackingFields: Boolean = false,
    custom: (ListProperty<String>.() -> Unit)? = null
) {
    compilerOptions {
        with(freeCompilerArgs) {
            if (expectActualClasses) {
            }

            if (contextParameters) {
            }

            if (explicitBackingFields) {
            }

            custom?.invoke(this)
        }
    }
}