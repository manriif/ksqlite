/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import komple.exec.addEnvironments
import komple.platform.Platform
import komple.project.c.COptimization
import komple.project.c.CProject

plugins {
    alias(libs.plugins.android.multiplatformLibrary) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka) apply true
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false

    alias(kompleLibs.plugins.komple)
    alias(kompleLibs.plugins.tool.androidNdk)
    alias(kompleLibs.plugins.tool.appleXcode)
    alias(kompleLibs.plugins.tool.cmake)
    alias(kompleLibs.plugins.tool.emscripten)
    alias(kompleLibs.plugins.tool.gnuSed)
    alias(kompleLibs.plugins.tool.jextract)
    alias(kompleLibs.plugins.tool.wabt)
    alias(kompleLibs.plugins.tool.zig)

    alias(libs.plugins.ksqlite)
}

allprojects {
    group = providers.gradleProperty("project.group").get()
    version = rootProject.libs.versions.ksqlite.get()
}

dependencies {
    dokka(projects.ksqliteGradlePlugin)
    dokka(projects.ksqliteCapi)
    dokka(projects.ksqliteKapi)
    dokka(projects.ksqliteWasmResources)
    dokka(projects.ksqliteForeign.ksqliteForeignCinterop)
    dokka(projects.ksqliteForeign.ksqliteForeignFfm)
    dokka(projects.ksqliteForeign.ksqliteForeignJni)
    dokka(projects.ksqliteForeign.ksqliteForeignWasm)
    dokka(projects.ksqliteTypes.ksqliteTypesCore)
    dokka(projects.ksqliteTypes.ksqliteTypesInternal)
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

    zig {
        compilationParams {
            linuxGlibcVersionMin = libs.versions.linux.glibc.min
            windowsVersionMin = libs.versions.windows.min
        }
    }

    sqlite {
        version = libs.versions.sqlite
        releaseYear = libs.versions.sqliteReleaseYear
        checksum = providers.gradleProperty("checksum.sqlite")
        ksqliteDirectory = ksqlite.ksqliteDirectory
        sqliteMcDirectory = tools.sqliteMultipleCiphers.installDirectory
    }

    sqliteMultipleCiphers {
        version = libs.versions.sqliteMultipleCiphers
        sqliteVersion = sqlite.version
        checksum = providers.gradleProperty("checksum.sqlitemc")
    }

    tools {
        wabt dependsOn cmake
    }

    execEnvironments {
        register("wasm") {
            addEnvironments(
                tools.emscripten,
                tools.gnuSed,
                tools.wabt
            )
        }
    }

    projects {
        register<CProject>(providers.gradleProperty("project.name").get()) {
            packageName = "ksqlite.foreign"
            libraryName = ksqlite.libraryName
            headerFile = ksqlite.ksqliteDirectory.file(cHeaderFile(KSQLITE))
            definitions = SqliteDefinitions
            optimization = COptimization.Level2

            headerFilters.from(
                headerFile,
                ksqlite.sqliteDirectory.file(cHeaderFile(SQLITE3))
            )

            sourceFiles.from(
                ksqlite.ksqliteDirectory.file(cSourceFile(KSQLITE)),
                ksqlite.sqliteDirectory.file(cSourceFile(SQLITE3))
            )

            includeDirectories.from(
                ksqlite.ksqliteDirectory,
                ksqlite.sqliteDirectory
            )

            compilerOptions.addAll(SqliteCompilerOptions)

            Platform.run {
                listOf(androidArm32, androidArm64, androidX86, androidX64).forEach { platform ->
                    optimization(platform, COptimization.Size)

                    linkerOptions(platform) {
                        addAll(SqliteAndroidLinkerOptions)
                    }
                }

                listOf(linuxArm64, linuxX64, macosArm64, macosX64).forEach { platform ->
                    linkerOptions(platform) {
                        addAll(SqliteUnixLinkerOptions)
                    }
                }
            }
        }
    }
}

dokka {
    dokkaPublications.html {
        moduleName = providers.gradleProperty("project.name")
        moduleVersion = project.version.toString()
        outputDirectory = rootDir.resolve("docs/api")
        failOnWarning = true
        suppressInheritedMembers = false
        suppressObviousFunctions = true
        offlineMode = false
    }

    pluginsConfiguration.html {
        val interceptionYear = providers.gradleProperty("project.inceptionYear")
        val devName = providers.gradleProperty("project.dev.name")

        footerMessage = interceptionYear.zip(devName) { year, name ->
            "© $year $name"
        }
    }
}

if (ksqlite.build.isDokka) {
    listOf(
        komple.tools.androidNdk,
        komple.tools.zig,
    ).forEach { tool ->
        tool.disableInstallationTasks()
    }
}