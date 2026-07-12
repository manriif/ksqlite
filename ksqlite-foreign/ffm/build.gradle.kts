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
import komple.gradle.project.c.CLibrary
import komple.platform.Platform
import komple.project.c.CLibraryType
import modules.ksqliteFfmResourceLibDirectory
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import tasks.GenerateFfmSourcesTask

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

val generatedSourceDirectory = layout.buildDirectory.dir("generated/ksqlite/src/jvmMain")

val libraries = Platform.run {
    listOf(
        linuxArm64,
        linuxX64,
        macosArm64,
        macosX64,
        mingwArm64,
        mingwX64
    ).map { platform ->
        komple.projects.kotlinSqlite.createLibrary(CLibraryType.Shared, platform)
    }
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

val generateFfmSources = tasks.registerKsqlite<GenerateFfmSourcesTask>("generateFfmSources") {
    outputDirectory = generatedSourceDirectory.map { it.dir("kotlin") }
    compilations = libraries.map(CLibrary::compilation)

    val cProject = komple.projects.kotlinSqlite.kProject
    libraryName = cProject.libraryName
    packageName = cProject.packageName
}

val copyJavaBindings = tasks.registerKsqlite<Copy>("copyJavaBindings") {
    from(javaBindings.map { it.generateDirectory })
    into(generatedSourceDirectory.map { it.dir("java") })
}

val generateSources = tasks.registerKsqlite<DefaultTask>("generateSources") {
    dependsOn(generateFfmSources)
    dependsOn(copyJavaBindings)
}

registerTaskForIde(generateSources)

kotlin {
    jvmTargets().forEach { target ->
        target.configureJvmTarget()
    }

    sourceSets.jvmMain {
        kotlin.srcDirs(generateFfmSources, copyJavaBindings)
    }
}

fun KotlinJvmTarget.configureJvmTarget() {
    compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME) {
        checkNotNull(compileJavaTaskProvider).configure {
            source(copyJavaBindings)
        }

        tasks.named<ProcessResources>(processResourcesTaskName).configure {
            libraries.forEach { library ->
                from(library.libraryFile) {
                    into(library.compilation.platform.map { platform ->
                        platform.ksqliteFfmResourceLibDirectory()
                    })
                }
            }
        }
    }
}