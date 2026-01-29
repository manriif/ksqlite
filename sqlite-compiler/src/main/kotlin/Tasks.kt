import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val sqliteCompilerTaskGroup = "sqlite compile"

const val TASK_SQLITE_DOWNLOAD = "sqliteDownload"
const val TASK_SQLITE_CHECKSUM = "sqliteChecksum"
const val TASK_SQLITE_UNZIP = "sqliteUnzip"
const val TASK_SQLITE_COMPILE = "sqliteCompile"
const val TASK_SQLITE_COMPILE_ALL_NATIVE_TARGETS = "sqliteCompileAllNativeTargets"

///////////////////////////////////////////////////////////////////////////
// Root tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers the task for downloading sqlite sources.
 */
fun Project.registerTasks(extension: SqliteCompilerExtension) {
    val downloadTaskProvider = registerSqliteDownloadTask(extension)
    val checksumTaskProvider = registerSqliteChecksumTask(extension, downloadTaskProvider)
    registerSqliteUnzipTask(extension, checksumTaskProvider)
}

/**
 * Registers and returns the task responsible for downloading SQLite sources.
 */
private fun Project.registerSqliteDownloadTask(
    extension: SqliteCompilerExtension
): TaskProvider<Download> = tasks.register<Download>(TASK_SQLITE_DOWNLOAD) {
    group = sqliteCompilerTaskGroup

    val amalgamationFileName = extension.sqliteRelease.map {
        "sqlite3mc-${it.sqliteMultipleCiphersVersion}-sqlite-${it.sqliteVersion}-amalgamation.zip"
    }

    src(extension.sqliteRelease.zip(amalgamationFileName) { release, fileName ->
        "https://github.com/utelle/SQLite3MultipleCiphers/releases/download/" +
                "v${release.sqliteMultipleCiphersVersion}/$fileName"
    })

    dest(extension.sqliteDownloadDirectory.zip(amalgamationFileName) { directory, fileName ->
        directory.file(fileName)
    })

    overwrite(false)
}

/**
 * Registers and returns the task responsible for checking downloaded SQLite sources.
 */
private fun Project.registerSqliteChecksumTask(
    extension: SqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Verify> = tasks.register<Verify>(TASK_SQLITE_CHECKSUM) {
    group = sqliteCompilerTaskGroup

    val amalgamationFile = downloadTaskProvider.map { it.dest }
    val checksum = extension.sqliteRelease.map { it.sha256checksum }

    // Implicit dependency
    inputs.file(amalgamationFile)
    inputs.property("checksum", checksum)

    src(amalgamationFile)
    algorithm("SHA-256")

    // Unfortunately, verify task do not accept provider
    checksum(checksum.get())
}

/**
 * Registers and returns the task responsible for unzipping the downloaded SQLite sources.
 */
@Suppress("NewApi")
private fun Project.registerSqliteUnzipTask(
    extension: SqliteCompilerExtension,
    checksumTaskProvider: TaskProvider<Verify>
): TaskProvider<Copy> = tasks.register<Copy>(TASK_SQLITE_UNZIP) {
    group = sqliteCompilerTaskGroup

    val fileOperations = serviceOf<FileSystemOperations>()
    val amalgamationFile = checksumTaskProvider.map { it.inputs.files.singleFile }
    val amalgamationZipTree = zipTree(amalgamationFile)

    // Implicit dependency on the checksum task
    inputs.file(amalgamationFile)
    outputs.dir(extension.sqliteSourcesDirectory)

    doFirst {
        fileOperations.delete {
            delete(extension.sqliteSourcesDirectory)
        }
    }

    from(amalgamationZipTree)
    into(extension.sqliteSourcesDirectory)
}

///////////////////////////////////////////////////////////////////////////
// Compilation tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the artifacts and cinterop definition
 * file for [nativeTarget].
 */
fun SqliteCompilerExtension.registerSqliteCompileNativeTargetTask(
    nativeTarget: KotlinNativeTarget,
    packageName: String,
    libraryDirectoryProvider: Provider<Directory>,
    defFileProvider: Provider<RegularFile>
): TaskProvider<Task> = nativeTarget.project.tasks.register(
    name = "$TASK_SQLITE_COMPILE${nativeTarget.name.uppercaseFirstChar()}"
) {
    group = sqliteCompilerTaskGroup

    // Explicit dependency on the unzip task
    dependsOn(nativeTarget.project.rootProject.tasks.named(TASK_SQLITE_UNZIP))

    val inputComponents = sqliteSourcesDirectory.zip(sqliteRelease) { directory, release ->
        directory to release.sqliteMcName
    }

    val outputComponents = libraryDirectoryProvider.zip(sqliteRelease) { directory, release ->
        directory to release.sqliteName
    }

    val headerFileProvider = inputComponents.map { it.first.file("${it.second}.h") }
    val sourceFileProvider = inputComponents.map { it.first.file("${it.second}.c") }

    // Implicit dependency on the source task
    inputs.files(headerFileProvider, sourceFileProvider)
    outputs.dir(libraryDirectoryProvider)

    val objectFileProvider = outputComponents.map { it.first.file("${it.second}.o") }
    val libraryFileProvider = outputComponents.map { it.first.file("lib${it.second}.a") }
    val compilerFlags = getNativeCompilerFlags(nativeTarget.konanTarget)
    val compiler = getNativeCompiler(nativeTarget.konanTarget)
    val archiver = getNativeArchiver(nativeTarget.konanTarget)

    val fileOperations = project.serviceOf<FileSystemOperations>()
    val execOperations = project.serviceOf<ExecOperations>()

    doFirst {
        fileOperations.delete {
            delete(libraryDirectoryProvider)
            delete(defFileProvider)
        }

        libraryDirectoryProvider.get().asFile.mkdirs()
    }

    doLast {
        val sourceFile = sourceFileProvider.get().asFile
        val objectFile = objectFileProvider.get().asFile
        val libraryFile = libraryFileProvider.get().asFile

        execOperations.exec {
            commandLine(
                compiler,
                *compilerFlags.toTypedArray(),
                "-c",
                sourceFile.absolutePath,
                "-o",
                objectFile.absolutePath,
                *SqliteCompileTimeOptions,
                "-O3"
            )
        }

        execOperations.exec {
            commandLine(archiver, "rcs", libraryFile.absolutePath, objectFile.absolutePath)
        }

        defFileProvider.get().asFile.writeText(
            createDefContent(packageName, libraryFile, sqliteRelease.get())
        )
    }
}

/**
 * Registers and returns the task responsible for generating the artifacts for all targets.
 */
fun Project.registerSqliteCompileAllNativeTargetsTask(): TaskProvider<Task> = tasks.register(
    name = TASK_SQLITE_COMPILE_ALL_NATIVE_TARGETS
) {
    group = sqliteCompilerTaskGroup
}