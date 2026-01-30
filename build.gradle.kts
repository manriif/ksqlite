import compilation.SqliteCompilationParameters
import toolchains.Toolchain
import toolchains.Toolchains
import utils.absolutePath
import java.util.Properties

plugins {
    alias(libs.plugins.android.multiplatformLibrary) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka) apply false // true
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.gradle.pluginPublish) apply false
    alias(libs.plugins.ksqliteCompiler)
}

allprojects {
    group = property("project.group").toString()
    version = rootProject.libs.versions.ksqlite.get()
}

ksqliteCompiler {
    downloadDirectory = layout.buildDirectory.dir("tmp/ksqlite")
    toolchainsDirectory = layout.buildDirectory.dir("toolchains")
    sqliteSourcesDirectory = layout.buildDirectory.dir("sqlite")

    compilationParams = SqliteCompilationParameters(
        sqliteVersion = libs.versions.ksqlite.get(),
        sqliteMCVersion = libs.versions.sqliteMultipleCiphers.get(),
        androidSdkMin = libs.versions.android.sdk.min.get(),
        macosVersionMin = libs.versions.macos.version.min.get(),
        iosVersionMin = libs.versions.ios.version.min.get(),
        tvosVersionMin = libs.versions.tvos.version.min.get(),
        watchosVersionMin = libs.versions.watchos.version.min.get(),
        toolchains = Toolchains(
            android = Toolchain(
                version = libs.versions.toolchain.android.get(),
                path = toolchainsDirectory.absolutePath("android-ndk")
            )
        ),
    )

    checksums = Properties()
        .apply {
            file("checksums.properties").inputStream().use { load(it) }
        }
        .run {
            KsqliteChecksums(
                androidNdkLinux = getProperty("android.ndk.linux"),
                androidNdkMacos = getProperty("android.ndk.macos"),
                androidNdkWindows = getProperty("android.ndk.windows"),
                sqliteMultipleCiphers = getProperty("sqlitemc")
            )
        }
}