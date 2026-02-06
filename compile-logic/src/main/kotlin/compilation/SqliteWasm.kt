package compilation

import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import platform.Host
import platform.OperatingSystem
import java.io.File
import kotlin.io.path.createTempDirectory

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

    // Use temporary directory for sqlite source tree to not write the original directory which will
    // break Gradle caching
    val sqliteSourceTree = createTempDirectory("ksqlite").toFile()

    fileOperations.copy {
        from(sqliteDirectory)
        into(sqliteSourceTree)
    }

    val wabtPath = wabtDirectory.resolve("bin")
    val sedPath = gnuSedDirectory.resolve("bin")
    val emsdkEnv = emscriptenDirectory.resolve("emsdk_env.sh").absolutePath
    val sqliteSourceFile = sqliteSourceTree.resolve("${params.sqliteMcAmalgamationName}.c")
    val wasmDirectory = sqliteSourceTree.resolve("ext/wasm")

    execOperations.exec {
        workingDir = sqliteSourceTree
        environment("PATH", "$wabtPath:${System.getenv("PATH")}")
        commandLine("bash", "-c", "source $emsdkEnv && ./configure")
    }

    execOperations.exec {
        workingDir = wasmDirectory
        environment("PATH", "$sedPath:${System.getenv("PATH")}")

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