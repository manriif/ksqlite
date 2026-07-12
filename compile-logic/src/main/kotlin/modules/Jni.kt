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
package modules

import SqliteAndroidLinkerOptions
import komple.project.c.CProject
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Build
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the arguments for CMake.
 */
fun CProject.cmakeArguments(): List<String> {
    val sourceFilesPaths = sourceFiles
        .joinToString(";", transform = File::getAbsolutePath)

    val includeDirectoriesPaths = includeDirectories
        .joinToString(";", transform = File::getAbsolutePath)

    val compileDefinitions = definitions.get().entries
        .joinToString(";") { "${it.key}=${it.value}" }

    val linkerOptions = SqliteAndroidLinkerOptions
        .joinToString(";")

    return listOf(
        "-DKSQLITE_LIB_NAME=${libraryName.get()}",
        "-DKSQLITE_INCLUDES=$includeDirectoriesPaths",
        "-DKSQLITE_SOURCES=$sourceFilesPaths",
        "-DKSQLITE_DEFINITIONS=$compileDefinitions",
        "-DKSQLITE_LINKER_OPTIONS=$linkerOptions"
    )
}

///////////////////////////////////////////////////////////////////////////
// Sources
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the content of the JNI runtime metadata.
 */
fun createSqliteJniRuntimeMetadataContent(
    packageName: String,
    libraryName: String
): String = """
    |package $packageName
    |
    |/**
    | * Name of the Ksqlite native library.
    | */
    |public const val KSQLITE_NATIVE_LIB_NAME: String = "$libraryName"
""".trimMargin()