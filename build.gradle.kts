import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    alias(libs.plugins.android.multiplatformLibrary) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka) apply false // true
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.gradle.pluginPublish) apply false
}

allprojects {
    group = property("project.group").toString()
    version = rootProject.libs.versions.ksqlite.get()
    //extra["isModule"] = path.startsWith(":modules")

    val dokkaBase = """{
        "footerMessage": "© 2024 <a href=\"https://github.com/manriif\">Maanrifa Bacar Ali</a>."
    }"""

    tasks.withType<AbstractDokkaTask>().configureEach {
        pluginsMapConfiguration = mapOf("org.jetbrains.dokka.base.DokkaBase" to dokkaBase)
    }
}

tasks.withType<DokkaMultiModuleTask> {
    val dokkaDir = rootProject.layout.projectDirectory.dir("dokka")

    includes = dokkaDir.files("README.md")
    moduleName = rootProject.property("project.name").toString()
    outputDirectory = dokkaDir.dir("documentation")
}