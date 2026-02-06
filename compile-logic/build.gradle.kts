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
        register("ksqlite") {
            id = "ksqlite"
            implementationClass = "KsqlitePlugin"
        }
    }
}

dependencies {
    implementation(libs.undercouch.dowload)
}