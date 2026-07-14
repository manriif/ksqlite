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

import KSQLITE
import KsqliteFunctions
import SQLITE3
import SQLITE_VERSION_FILE
import cHeaderFile
import cSourceFile
import copyToTempDirectory
import komple.exec.Command
import komple.exec.CommandExecutor
import org.gradle.api.file.FileSystemOperations
import replaceFiles
import replacePrefix
import sqlitePrefixed
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Sources
///////////////////////////////////////////////////////////////////////////

private const val GENERATED_ARTIFACTS = "artifacts"
private const val GENERATED_SOURCES = "sources"

private const val KSQLITE_AMALGAMATION = "${KSQLITE}_amalgamation"

private const val EXT_WASM_PATH = "ext/wasm"
private const val GNU_MAKEFILE = "${EXT_WASM_PATH}/GNUmakefile"
private const val EXT_WASM_API_PATH = "$EXT_WASM_PATH/api"
private const val PRE_JS_CPP_JS = "$EXT_WASM_API_PATH/pre-js.c-pp.js"
private const val POST_JS_FOOTER_JS = "$EXT_WASM_API_PATH/post-js-footer.js"
private const val EXPORTED_FUNCTIONS = "$EXT_WASM_API_PATH/EXPORTED_FUNCTIONS.c-pp"

private const val SQLITE3_64BIT = "$SQLITE3-64bit"

/**
 * Extra resources files that can be embedded in the library.
 */
private val WasmExtraResourceFileNames = listOf<String>(
    //"opfs-async-proxy.js"
).sqlitePrefixed('-')

/**
 * Extra functions which aren't exported by default in the wasm build.
 * Exports with care as theses aren't tested by the official wasm team.
 *
 * Some aren't that meaningful to use in web platforms but are exported to align at maximum with
 * other platforms and avoid plenty of expect/actual.
 */
private val WasmExtraExportedFunctions = KsqliteFunctions + listOf(
    "autovacuum_pages",
    "backup_finish",
    "backup_init",
    "backup_pagecount",
    "backup_remaining",
    "backup_step",
    "bind_blob64",
    "bind_text64",
    "bind_value",
    "bind_zeroblob64",
    "blob_bytes",
    "blob_close",
    "blob_open",
    "blob_read",
    "blob_reopen",
    "blob_write",
    "close",
    "config",
    "db_cacheflush",
    "db_config",
    "db_release_memory",
    "filename_database",
    "filename_journal",
    "filename_wal",
    "hard_heap_limit64",
    "log",
    "memory_used",
    "memory_highwater",
    "mprintf",
    "release_memory",
    "result_blob64",
    "result_text64",
    "result_value",
    "snapshot_cmp",
    "snapshot_free",
    "snapshot_get",
    "snapshot_open",
    "snapshot_recover",
    "soft_heap_limit64",
    "system_errno",
    "threadsafe",
    "value_encoding",
    "vtab_config",
    "wal_autocheckpoint",
    "wal_checkpoint",
    "wal_checkpoint_v2",
    "wal_hook"
).sqlitePrefixed()

/**
 * Performs some adjustments and fixes for WASM compilation.
 */
fun configureSqliteWasmTrunk(
    ksqliteDirectory: File,
    sqliteDirectory: File
) {
    replaceFiles(
        sourceDirectory = ksqliteDirectory,
        destinationDirectory = sqliteDirectory,
        POST_JS_FOOTER_JS,
        PRE_JS_CPP_JS,
        GNU_MAKEFILE
    )

    val exportedFunctionFile = sqliteDirectory.resolve(EXPORTED_FUNCTIONS)
    val defaultExportedFunctions = exportedFunctionFile.readText()

    sqliteDirectory.resolve(EXPORTED_FUNCTIONS).outputStream().bufferedWriter().use { output ->
        WasmExtraExportedFunctions.forEach { name ->
            output.appendLine("_$name")
        }

        output.write(defaultExportedFunctions)
    }
}

private fun File.mergeFiles(vararg files: File) {
    val separator = System.lineSeparator().encodeToByteArray()

    outputStream().use { output ->
        files.forEach { file ->
            file.inputStream().use { input ->
                input.copyTo(output)
            }

            output.write(separator)
        }
    }
}

private fun generateKsqliteAmalgamation(
    ksqliteDirectory: File,
    sqliteDirectory: File,
    ksqliteAmalgamationHeaderFile: File,
    ksqliteAmalgamationSourceFile: File,
) {
    val sqliteHeaderFile = sqliteDirectory.resolve(cHeaderFile(SQLITE3))
    val sqliteSourceFile = sqliteDirectory.resolve(cSourceFile(SQLITE3))
    val ksqliteHeaderFile = ksqliteDirectory.resolve(cHeaderFile(KSQLITE))
    val ksqliteSourceFile = ksqliteDirectory.resolve(cSourceFile(KSQLITE))

    ksqliteAmalgamationHeaderFile.mergeFiles(
        sqliteHeaderFile,
        ksqliteHeaderFile
    )

    ksqliteAmalgamationSourceFile.mergeFiles(
        sqliteSourceFile,
        ksqliteHeaderFile,
        ksqliteSourceFile
    )
}

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Compiles SQLite for Wasm to [outputDirectory].
 */
fun compileSqliteWasm(
    fileOperations: FileSystemOperations,
    commandExecutor: CommandExecutor,
    sqliteVersion: String,
    ksqliteDirectory: File,
    sqliteDirectory: File,
    outputDirectory: File
) {
    // A temporary directory is used to not write the original directory which will break Gradle
    // caching
    val sqliteDirectory = fileOperations.copyToTempDirectory(sqliteDirectory)
    val versionFile = sqliteDirectory.resolve(SQLITE_VERSION_FILE)

    // Restore the deleted version file as it is required for wasm build
    versionFile.writeText("${sqliteVersion}\n")

    commandExecutor.execute(
        command = Command("./configure"),
        workingDirectory = sqliteDirectory
    )

    val ksqliteAmalgamationHeaderFile = sqliteDirectory.resolve(cHeaderFile(KSQLITE_AMALGAMATION))
    val ksqliteAmalgamationSourceFile = sqliteDirectory.resolve(cSourceFile(KSQLITE_AMALGAMATION))
    val wasmDirectory = sqliteDirectory.resolve(EXT_WASM_PATH)

    generateKsqliteAmalgamation(
        ksqliteDirectory = ksqliteDirectory,
        sqliteDirectory = sqliteDirectory,
        ksqliteAmalgamationHeaderFile = ksqliteAmalgamationHeaderFile,
        ksqliteAmalgamationSourceFile = ksqliteAmalgamationSourceFile
    )

    commandExecutor.execute(
        command = Command(
            "make",
            "-j4",
            "64bit",
            "api.oo1=0",
            "sqlite3.h=${ksqliteAmalgamationHeaderFile.absolutePath}",
            "sqlite3.c=${ksqliteAmalgamationSourceFile.absolutePath}"
        ),
        workingDirectory = wasmDirectory
    )

    val generatedWasmArtifactsDirectory = wasmDirectory.resolve("jswasm")
    val esm64Directory = generatedWasmArtifactsDirectory.resolve("esm64")
    val sqlite64BitMjs = esm64Directory.resolve("$SQLITE3_64BIT.mjs")

    // Replace hard-coded-generated 'sqlite3-64bit.wasm' to 'ksqlite.wasm'
    // TODO: make the GNUmakefile generate the ksqlite.wasm when it has been mastered
    val patchedContent = sqlite64BitMjs
        .readText()
        .replace("'$SQLITE3_64BIT.wasm'", "'$KSQLITE.wasm'")

    sqlite64BitMjs.writeText(patchedContent)

    fileOperations.copy {
        from(generatedWasmArtifactsDirectory)
        into(outputDirectory.resolve(GENERATED_ARTIFACTS))
    }

    // Just to visualize what have been used to compile wasm
    fileOperations.copy {
        from(ksqliteAmalgamationHeaderFile, ksqliteAmalgamationSourceFile)
        into(outputDirectory.resolve(GENERATED_SOURCES))
    }
}

/**
 * Copies the resources previously generated by [compileSqliteWasm] to [outputDirectory].
 */
fun copySqliteWasmGeneratedResources(
    fileOperations: FileSystemOperations,
    inputDirectory: File,
    outputDirectory: File,
) {
    val artifactsDirectory = inputDirectory.resolve(GENERATED_ARTIFACTS)
    val esm64Directory = artifactsDirectory.resolve("esm64")

    fileOperations.copy {
        from(esm64Directory) {
            include { it.name.startsWith(SQLITE3_64BIT) }
            replacePrefix(SQLITE3_64BIT, KSQLITE)
        }

        from(artifactsDirectory) {
            include { !it.isDirectory && it.name in WasmExtraResourceFileNames }
        }

        into(outputDirectory)
    }
}