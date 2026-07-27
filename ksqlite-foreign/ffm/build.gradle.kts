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
import komple.platform.Platform
import komple.project.c.CLibraryType
import modules.ksqliteFfmResourceLibDirectory
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import tasks.GenerateFfmSourcesTask
import tasks.GenerateFileChecksumTask

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val platforms = if (ksqlite.build.isDokka) {
    emptyList()
} else {
    Platform.run {
        listOf(
            linuxArm64,
            linuxX64,
            macosArm64,
            macosX64,
            mingwArm64,
            mingwX64
        )
    }.filter { platform ->
        platform in ksqlite.build.enabledPlatforms
    }
}

val libraryWithChecksums = platforms.associate { platform ->
    val library = komple.projects.kotlinSqlite.createLibrary(CLibraryType.Shared, platform)
    val osName = platform.operatingSystem.altName.uppercaseFirstChar()
    val archName = platform.architecture.altName.uppercaseFirstChar()
    val checksumTaskName = "generate${osName}${archName}ChecksumFfm"

    val checksum = tasks.registerKsqlite<GenerateFileChecksumTask>(checksumTaskName) {
        this.inputFile = library.libraryFile

        this.checksumFile = layout.buildDirectory
            .dir("ksqlite/checksums/${platform.altName}")
            .zip(library.libraryFile) { directory, file ->
                directory.file("${file.asFile.name}.sha256")
            }
    }

    library to checksum
}

val javaBindings = komple.projects.kotlinSqlite.jextract.bindingGenerators.register(KSQLITE) {
    options {
        headerClassName = SQLITE3
        includeConstants = SqliteConstants
        includeFunctions = KsqliteFunctions + sqliteFunctions(true)
        includeStructs = SqliteStructs
        includeTypedefs = KsqliteTypedefs
    }
}

val javaBindingsSources = javaBindings.flatMap { it.generateDirectory }

val generateFfmSources = tasks.registerKsqlite<GenerateFfmSourcesTask>("generateFfmSources") {
    outputDirectory = layout.buildDirectory.dir("generated/ksqlite/src/jvmMain/kotlin")
    compilations = libraryWithChecksums.map { it.key.compilation }

    val cProject = komple.projects.kotlinSqlite.kProject
    libraryName = cProject.libraryName
    packageName = cProject.packageName
}

val generateSources = tasks.registerKsqlite<DefaultTask>("generateSources") {
    dependsOn(javaBindings.flatMap { it.generateTaskProvider })
    dependsOn(generateFfmSources)
}

registerTaskForIde(generateSources)

kotlin {
    jvmToolchainFfm()

    jvmTargets().forEach { target ->
        target.configureJvmTarget()
    }

    sourceSets.jvmMain {
        @Suppress("OPT_IN_USAGE")
        generatedKotlin.srcDirs(
            generateFfmSources,
            javaBindingsSources
        )
    }
}

fun KotlinJvmTarget.configureJvmTarget() {
    compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME).configure {
        checkNotNull(compileJavaTaskProvider).configure {
            source(javaBindingsSources)
        }

        tasks.named<ProcessResources>(processResourcesTaskName).configure {
            libraryWithChecksums.forEach { (library, checksum) ->
                from(objects.fileCollection().from(library.libraryFile, checksum)) {
                    into(library.compilation.platform.map { platform ->
                        platform.ksqliteFfmResourceLibDirectory()
                    })
                }
            }
        }
    }
}