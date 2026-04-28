import komple.project.c.CProject

/*import compilation.SqliteCompilationParameters
import tools.Toolchains
import tools.Tool
import tools.Tools
import utils.absolutePath
import java.util.Properties*/

plugins {
    alias(libs.plugins.android.multiplatformLibrary) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka) apply false // true
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.gradle.pluginPublish) apply false
    alias(libs.plugins.ksqlite)

    alias(kompleLibs.plugins.komple)
    alias(kompleLibs.plugins.tool.androidNdk)
    alias(kompleLibs.plugins.tool.appleXcode)
    alias(kompleLibs.plugins.tool.emscripten)
    alias(kompleLibs.plugins.tool.cmake)
    alias(kompleLibs.plugins.tool.gnuSed)
    alias(kompleLibs.plugins.tool.jextract)
    alias(kompleLibs.plugins.tool.wabt)
}

allprojects {
    group = property("project.group").toString()
    version = rootProject.libs.versions.ksqlite.get()
}

komple {
    //registerTool<SqliteMCConfigurator>("Sqlite Multiple Ciphers")
    //registerTool("Sqlite", KompleToolConfigurator::class)

    projects {
        register<CProject>("ksqlite", CProject::configureKsqliteProject)
    }

    tools {
        wabt dependsOn cmake
    }

    androidNdk {
        compilationParams {
            minSdk = libs.versions.android.sdk.min
        }
    }

    appleXcode {
        compilationParams {
            versionMinMacos = libs.versions.apple.macos.min
            versionMinIos = libs.versions.apple.ios.min
            versionMinTvos = libs.versions.apple.tvos.min
            versionMinWatchos = libs.versions.apple.watchos.min
        }
    }
}

private fun CProject.configureKsqliteProject() {
    libraryName = "ksqlite"

}

/*ksqlite {
    val ksqliteBuildDir = layout.buildDirectory.dir("ksqlite")
    ksqliteDirectory = layout.projectDirectory.dir("ksqlite")

    checksums = Properties()
        .apply { file("komple.version.txt").inputStream().use { load(it) } }
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
                emsdk = getProperty("emsdk"),
                wabt = getProperty("wabt"),
                gnuSed = getProperty("gnu.sed")
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
        toolchains = ksqliteBuildDir.map { it.dir("toolchains") }.run {
            Toolchains(
                android = Tool(
                    version = libs.versions.toolchain.android.get(),
                    path = absolutePath("android-ndk")
                )
            )
        },
    )


    downloadDirectory = layout.buildDirectory.dir("tmp/ksqlite")
    sqliteReleaseYear = libs.versions.sqliteReleaseYear.get()
    sqliteSourcesDirectory = ksqliteBuildDir.map { it.dir("sqlite") }
    jdkVersion = libs.versions.jvm.toolchain.get()
}*/