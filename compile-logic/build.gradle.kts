import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

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
    implementation(libs.undercouch.dowload)
    implementation(kompleLibs.komple)
}