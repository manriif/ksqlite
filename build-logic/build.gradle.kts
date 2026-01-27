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
        listOf("common", "dokka", "kmp", "publish").forEach { scriptName ->
            named("conventions-$scriptName") {
                version = libs.versions.ksqlite.get()
            }
        }
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.dokka.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.vanniktech.mavenPublish)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}