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
    if (Host.Current.operatingSystem == OperatingSystem.Windows) {
        throw UnsupportedOperationException(
            "Windows is currently not supported for wasm compilation"
        )
    }

    val wabtPath = "wabt-1.0.39/bin"
    val sedPath = "sed-4.9/build/bin"
    val emsdkEnv = emscriptenDirectory.resolve("emsdk_env.sh").absolutePath
    val sqliteSourceFile = sqliteDirectory.resolve("${params.sqliteMcAmalgamationName}.c")
    val wasmDirectory = sqliteDirectory.resolve("ext/wasm")

    val makeCommand = listOf(
        "make",
        //"-j4",
        //"64bit",
        "${params.sqliteName}.c=${sqliteSourceFile.absolutePath}"
    ).joinToString(" ")

    execOperations.exec {
        workingDir = sqliteDirectory
        environment("PATH", "$wabtPath:${System.getenv("PATH")}")
        commandLine("bash", "-c", "source $emsdkEnv && ./configure")
    }

    execOperations.exec {
        workingDir = wasmDirectory
        environment("PATH", "$sedPath:${System.getenv("PATH")}")
        commandLine(
            "make",
            "-j8",
            "64bit",
            "${params.sqliteName}.c=${sqliteSourceFile.absolutePath}")
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