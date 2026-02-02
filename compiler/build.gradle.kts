plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvm.toolchain.get())
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