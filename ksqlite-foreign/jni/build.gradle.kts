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
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import modules.cmakeArguments
import tasks.GenerateJniSourcesTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.conventions.common)
    alias(kompleLibs.plugins.komple)
}

val generateJniSources = tasks.registerKsqlite<GenerateJniSourcesTask>("generateJniSources") {
    outputDirectory = layout.buildDirectory.dir("generated/ksqlite/src/main/kotlin")

    val cProject = komple.projects.kotlinSqlite.kProject
    libraryName = cProject.libraryName
    packageName = cProject.packageName
}

registerTaskForIde(generateJniSources)

kotlin {
    configureKotlin()
}

android {
    namespace = localNamespace

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.toolchain.min.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.toolchain.min.get())
    }

    compileSdk {
        version = release(libs.versions.android.sdk.compile.get().toInt())
    }

    defaultConfig {
        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                arguments += komple.projects.kotlinSqlite.kProject.cmakeArguments()
            }
        }
    }

    externalNativeBuild {
        ndkVersion = libs.versions.android.ndk.get()

        cmake {
            version = libs.versions.cmake.get()
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    androidComponents {
        onVariants { variant ->
            variant.sources.kotlin?.addGeneratedSourceDirectory(
                taskProvider = generateJniSources,
                wiredWith = GenerateJniSourcesTask::outputDirectory
            )
        }
    }
}

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            javadocJar = JavadocJar.Empty(),
            sourcesJar = SourcesJar.Sources()
        )
    )
}