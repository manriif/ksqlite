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
            implementationClass = "sqlite.SqliteCompilerPlugin"
        }
    }
}

dependencies {
    implementation(libs.undercouch.dowload)
}