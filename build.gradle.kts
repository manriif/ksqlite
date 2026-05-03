import komple.platform.Platform
import komple.project.c.COptimization
import komple.project.c.CProject

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

ksqlite {
    ksqliteDirectory = layout.projectDirectory.dir("ksqlite")
    sqliteDirectory = komple.tools.sqlite.installDirectory
    libraryName = KSQLITE
}

komple {
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

    sqlite {
        version = libs.versions.sqlite
        releaseYear = libs.versions.sqliteReleaseYear
        checksum = property("checksum.sqlite").toString()
        ksqliteDirectory = ksqlite.ksqliteDirectory
        sqliteMcDirectory = tools.sqliteMultipleCiphers.installDirectory
    }

    sqliteMultipleCiphers {
        version = libs.versions.sqliteMultipleCiphers
        sqliteVersion = libs.versions.sqlite
        checksum = property("checksum.sqlitemc").toString()
    }

    tools {
        wabt dependsOn cmake
    }

    execEnvironments {
        register("wasm") {
            tools {
                addEnvironments(emscripten, gnuSed, wabt)
            }
        }
    }

    projects {
        register<CProject>("Kotlin SQLite") {
            packageName = property("project.namespace").toString()
            libraryName = ksqlite.libraryName
            headerFile = ksqlite.ksqliteDirectory.file(cHeaderFile(KSQLITE))
            definitions = SqliteDefinitions
            optimization = COptimization.Level2

            headerFilters.from(
                headerFile,
                ksqlite.sqliteDirectory.file(cHeaderFile(SQLITE3_MC_AMALGAMATION))
            )

            sourceFiles.from(
                ksqlite.ksqliteDirectory.file(cSourceFile(KSQLITE)),
                ksqlite.sqliteDirectory.file(cSourceFile(SQLITE3_MC_AMALGAMATION))
            )

            includeDirectories.from(
                ksqlite.ksqliteDirectory,
                ksqlite.sqliteDirectory
            )

            Platform.run {
                listOf(linuxArm64, linuxX64, macosArm64, macosX64).forEach { platform ->
                    linkerOptions(platform) {
                        addAll(SqliteUnixLinkerOptions)
                    }
                }
            }
        }
    }
}