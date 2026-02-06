package compilation

import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import platform.Host
import platform.OperatingSystem
import utils.copyToTempDirectory
import java.io.File

/**
 * Compiles SQLite for Wasm.
 */
fun compileSqliteWasm(
    fileOperations: FileSystemOperations,
    execOperations: ExecOperations,
    sqliteDirectory: File,
    emscriptenDirectory: File,
    gnuSedDirectory: File,
    wabtDirectory: File,
    outputDirectory: File,
    params: SqliteCompilationParameters,
) {
    if (Host.Current.operatingSystem == OperatingSystem.Windows) {
        throw UnsupportedOperationException(
            "Windows is currently not supported for SQLite WASM compilation"
        )
    }

    // Use temporary directory for sqlite source tree and emscripten to not write the original
    // directories which will break Gradle caching
    val sqliteDirectory = fileOperations.copyToTempDirectory(sqliteDirectory)
    val emscriptenDirectory = fileOperations.copyToTempDirectory(emscriptenDirectory)
    val wabtBin = wabtDirectory.resolve("bin")
    val gnuSedBin = gnuSedDirectory.resolve("bin")
    val emsdkEnv = emscriptenDirectory.resolve("emsdk_env.sh").absolutePath
    val sqliteSourceFile = sqliteDirectory.resolve("${params.sqliteMcAmalgamationName}.c")
    val wasmDirectory = sqliteDirectory.resolve("ext/wasm")

    execOperations.exec {
        workingDir = sqliteDirectory
        environment("PATH", "$wabtBin:${System.getenv("PATH")}")
        commandLine("bash", "-c", "source $emsdkEnv && ./configure")
    }

    execOperations.exec {
        workingDir = wasmDirectory
        environment("PATH", "$gnuSedBin:${System.getenv("PATH")}")

        commandLine(
            "make",
            "-j4",
            "64bit",
            "${params.sqliteName}.c=${sqliteSourceFile.absolutePath}"
        )
    }

    val generatedOutputDirectory = wasmDirectory.resolve("jswasm")

    fileOperations.copy {
        from(generatedOutputDirectory)
        into(outputDirectory)

        exclude { element ->
            element.isDirectory && !element.name.startsWith("esm")
        }

        rename { name ->
            name.replace("-64bit", "")
        }
    }
}