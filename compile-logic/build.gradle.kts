plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvm.toolchain.get())
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

gradlePlugin {
    plugins {
        register("ksqlite") {
            id = "ksqlite"
            implementationClass = "KsqlitePlugin"
        }
    }
}

dependencies {
    implementation(kompleLibs.kompleGradlePlugin)
}