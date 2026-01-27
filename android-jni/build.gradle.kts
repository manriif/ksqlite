plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
}

kotlin {

}

android {
    namespace = localNamespace

    compileSdk {
        version = release(libs.versions.android.sdk.compile.get().toInt())
    }

    defaultConfig {
        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }
    }

    externalNativeBuild {
        cmake {

        }
    }

    /*compilations.configureEach {
        compileTaskProvider.configure {
            compilerOptions {
                jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.target.get())
            }
        }
    }*/
}