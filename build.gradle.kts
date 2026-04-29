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
    projects {
        register<komple.project.c.CProject>("Kotlin SQLite") {
            libraryName = ksqlite.libraryName
        }
    }

    tools {
        wabt dependsOn cmake
    }

    commandExecutors {
        register("wasmExecutor") {
            execEnvironments.addAll(
                tools.run {
                    listOf(emscripten, gnuSed, wabt)
                        .map { it.execEnvironment }
                }
            )
        }
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
}