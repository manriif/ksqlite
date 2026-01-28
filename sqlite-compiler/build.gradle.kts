plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvm.target.get())
    }
}

gradlePlugin {
    plugins {
        register("sqlite-compiler") {
            id = "sqlite-compiler"
            implementationClass = "SqliteCompilerPlugin"
        }
    }
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.undercouch.dowload)
    implementation(libs.z4kn4fein.semver)
}