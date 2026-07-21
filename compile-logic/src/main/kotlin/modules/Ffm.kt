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

import komple.platform.Architecture.Arm64
import komple.platform.Architecture.X64
import komple.platform.OperatingSystem.Linux
import komple.platform.OperatingSystem.MacOS
import komple.platform.OperatingSystem.Windows
import komple.platform.Platform
import komple.project.c.CCompilation
import java.io.File

private const val NATIVE_LIBS_RESOURCE_DIR_NAME = "native"
private const val OS_NAME = "osName"
private const val OS_ARCH = "osArch"

/**
 * Returns the path to the directory where the generated shared library for `this` [Platform] should
 * be placed into relatively to the jar resources root.
 */
fun Platform.ksqliteFfmResourceLibDirectory(): String =
    "$NATIVE_LIBS_RESOURCE_DIR_NAME/$name"

/**
 * Returns the content supplied by [block] of a when entry for the current os and arch.
 */
private inline fun CCompilation.whenEntry(
    block: (
        platform: Platform,
        libFile: File
    ) -> String
): String {
    val platform = platform.get()
    val libFile = libraryFile.get().asFile

    val runtimeOsNameTest = when (platform.operatingSystem) {
        MacOS -> "isMacOs"
        Linux -> "isLinux"
        Windows -> "isWindows"
        else -> error("Non-desktop OSs aren't supported")
    }

    val runtimeOsArchTest = when (platform.architecture) {
        Arm64 -> "isArm64"
        X64 -> "isAmd64"
        else -> error("32-bit CPU architectures aren't supported")
    }

    return "$OS_NAME.$runtimeOsNameTest() && $OS_ARCH.$runtimeOsArchTest() -> " +
            block(platform, libFile)
}

/**
 * Returns the content of the FFM runtime metadata.
 */
fun generateKsqliteFfmRuntimeMetadataContent(
    packageName: String,
    libraryName: String,
    compilations: List<CCompilation>
): String = """
    |package $packageName
    |
    |/**
    | * Name of the Ksqlite native library.
    | */
    |internal const val KSQLITE_NATIVE_LIB_NAME: String = "$libraryName"
    |
    |/**
    | * Returns the path to the native library for [$OS_NAME] and [$OS_ARCH].
    | */
    |internal fun ksqliteLibPath($OS_NAME: String, $OS_ARCH: String): String = when {
    |${
    compilations.joinToString("\n") { compilation ->
        "    " + compilation.whenEntry { platform, libFile ->
            "\"${platform.ksqliteFfmResourceLibDirectory()}/${libFile.name}\""
        }
    }
}
    |    else -> error("Unsupported platform: $$OS_NAME $$OS_ARCH")
    |}
""".trimMargin()