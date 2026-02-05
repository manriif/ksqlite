package compilation

import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import platform.Host
import platform.OperatingSystem
import java.io.File

/**
 * Compiles SQLite for Wasm.
 */
fun compileSqliteWasm(
    fileOperations: FileSystemOperations,
    execOperations: ExecOperations,
    sqliteDirectory: File,
    emscriptenDirectory: File,
    outputDirectory: File,
    params: SqliteCompilationParameters,
) {
    val wasmDirectory = sqliteDirectory.resolve("ext/wasm")
    val sqliteSourceFile = sqliteDirectory.resolve("${params.sqliteMcAmalgamationName}.c")

    val makeCommand = listOf(
        "make",
        "-j4",
        //"64bit",
        "${params.sqliteName}.c=${sqliteSourceFile.absolutePath}"
    ).joinToString(" ")

    execOperations.exec {
        workingDir = sqliteDirectory

        when (Host.Current.operatingSystem) {
            OperatingSystem.Linux, OperatingSystem.MacOS -> {
                val env = emscriptenDirectory.resolve("emsdk_env.sh").absolutePath

                commandLine(
                    "bash",
                    "-c",
                    "source $env && ./configure && cd ./ext/wasm && $makeCommand"
                )
            }

            OperatingSystem.Windows -> {
                /*val env = emscriptenDirectory.resolve("emsdk_env.bat").absolutePath

                commandLine(
                    "cmd",
                    "/c",
                    "$env && $makeCommand"
                )*/
            }
        }
    }

    val generatedOutputDirectory = wasmDirectory.resolve("jswasm")

    fileOperations.copy {
        from(generatedOutputDirectory)
        into(outputDirectory)
    }

    /*fileOperations.delete {
        delete(generatedOutputDirectory)
    }*/
}