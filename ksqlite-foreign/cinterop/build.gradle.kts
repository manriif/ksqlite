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
import komple.gradle.kmp.toPlatform
import komple.project.c.CLibraryType
import modules.KsqliteNoStringConversions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(kompleLibs.plugins.komple)
}

kotlin {
    nativeTargets().forEach { target ->
        target.configureNativeTarget()
    }
}

fun KotlinNativeTarget.configureNativeTarget() {
    if (konanTarget.toPlatform() !in ksqlite.build.enabledPlatforms) {
        return
    }

    val library = komple.projects.kotlinSqlite.createLibrary(CLibraryType.Static, this) {
        generateDefFileTaskProvider.configure {
            dependsOn(komple.tools.sqlite.installTaskProvider)

            if (ksqlite.build.isDokka) {
                libraryFile = null as File?
            }
        }

        excludedFunctions = sqliteFunctions(false)
        noStringConversion = KsqliteNoStringConversions

        // TODO: Direct ccall mode seems not problematic for this library as we conntrol the C side
        //  as well. Waiting for feedbacks before removing or maintaining it.
        //
        // Faced during development :
        // - https://youtrack.jetbrains.com/issue/KT-82031: solved in ksqlite.h / ksqlite.c
        extraOpts("-Xccall-mode", "direct")
    }

    library.compileTaskProvider.configure {
        enabled = !ksqlite.build.isDokka
    }
}