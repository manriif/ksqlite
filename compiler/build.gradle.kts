plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvm.target.default.get())
    }
}

gradlePlugin {
    plugins {
        register("ksqlite-compiler") {
            id = "ksqlite-compiler"
            implementationClass = "KsqliteCompilerPlugin"
        }
    }
}

dependencies {
    implementation(libs.undercouch.dowload)
}