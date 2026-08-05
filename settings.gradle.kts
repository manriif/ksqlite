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
rootProject.name = "kotlin-sqlite"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    includeBuild("compile-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
        google()

        ivy {
            url = uri("https://nodejs.org/dist")
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }

        ivy {
            url = uri("https://github.com/yarnpkg/yarn/releases/download")
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
    }

    versionCatalogs {
        create("kompleLibs") {
            val kompleVersion = file("komple.version").readText()
            from("io.github.manriif.komple:komple-catalog:$kompleVersion")
        }
    }
}

fun includeNested(vararg paths: String) {
    val projectPath = ":${paths.joinToString(":")}"
    include(projectPath)
    project(projectPath).name = paths.joinToString("-")
}

include(":ksqlite-capi")
include(":ksqlite-gradle-plugin")
include(":ksqlite-kapi")
include(":ksqlite-structs")
include(":ksqlite-wasm-resources")
includeNested("ksqlite-foreign", "cinterop")
includeNested("ksqlite-foreign", "ffm")
includeNested("ksqlite-foreign", "jni")
includeNested("ksqlite-foreign", "wasm")
includeNested("ksqlite-internal", "runtime")
includeNested("ksqlite-internal", "test")
includeNested("ksqlite-types", "core")
includeNested("ksqlite-types", "internal")