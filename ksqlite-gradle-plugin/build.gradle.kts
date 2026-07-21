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
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.conventions.common)
}

val generateKsqliteVersion = tasks.registerKsqlite<GenerateFileTask>("generateKsqliteVersion") {
    outputFile = layout.buildDirectory.file("generated/resources/version.txt")
    content = libs.versions.ksqlite
}

tasks.processResources.configure {
    from(generateKsqliteVersion) {
        into(".")
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
}

kotlin {
    configureKotlin()

    compilerOptions {
        progressiveMode.set(false)
    }
}

gradlePlugin {
    website = projectWebsite
    vcsUrl = projectGitUrl

    plugins {
        create("ksqlite") {
            id = projectGroup
            implementationClass = "ksqlite.gradle.KsqlitePlugin"
            displayName = localName
            description = localDescription
            tags = localTags.split(',')
        }
    }
}