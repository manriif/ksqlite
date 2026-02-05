import compilation.SqliteCompilationParameters
import toolchains.Toolchain
import toolchains.Toolchains
import tools.Tool
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
    val ksqliteDir = layout.buildDirectory.dir("ksqlite")

    checksums = Properties()
        .apply { file("checksums.properties").inputStream().use { load(it) } }
        .run {
            KsqliteChecksums(
                androidNdkLinux = getProperty("android.ndk.linux"),
                androidNdkMacos = getProperty("android.ndk.macos"),
                androidNdkWindows = getProperty("android.ndk.windows"),
                jextractLinuxAarch64 = getProperty("jextract.linux.aarch64"),
                jextractLinuxX64 = getProperty("jextract.linux.x64"),
                jextractMacosAarch64 = getProperty("jextract.macos.aarch64"),
                jextractMacosX64 = getProperty("jextract.macos.x64"),
                jextractWindowsX64 = getProperty("jextract.windows.x64"),
                sqlite = getProperty("sqlite"),
                sqliteMc = getProperty("sqlitemc"),
                emsdk = getProperty("emsdk")
            )
        }

    compilationParams = SqliteCompilationParameters(
        libraryName = "ksqlite",
        sqliteVersion = libs.versions.ksqlite.get(),
        sqliteMCVersion = libs.versions.sqliteMultipleCiphers.get(),
        androidSdkMin = libs.versions.android.sdk.min.get(),
        macosVersionMin = libs.versions.macos.version.min.get(),
        iosVersionMin = libs.versions.ios.version.min.get(),
        tvosVersionMin = libs.versions.tvos.version.min.get(),
        watchosVersionMin = libs.versions.watchos.version.min.get(),
        toolchains = ksqliteDir.map { it.dir("toolchains") }.run {
            Toolchains(
                android = Toolchain(
                    version = libs.versions.toolchain.android.get(),
                    path = absolutePath("android-ndk")
                )
            )
        },
    )

    downloadDirectory = layout.buildDirectory.dir("tmp/ksqlite")
    sqliteSourcesDirectory = ksqliteDir.map { it.dir("sqlite") }
    jdkVersion = libs.versions.jvm.toolchain.get()

    emscripten = Tool(
        version = libs.versions.emscripten.get(),
        path = ksqliteDir.map { it.dir("emscripten") }
    )

    jextract = Tool(
        version = libs.versions.jextract.get(),
        path = ksqliteDir.map { it.dir("jextract") }
    )
}