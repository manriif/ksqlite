package tasks

import SqliteCompilerExtension
import compilation.SqliteStaticTarget
import compilation.libraryFile
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import interop.createDefContent
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import sqliteCompilerExtension

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val sqliteCompilerTaskGroup = "sqlite"

const val TASK_SQLITE_DOWNLOAD = "sqliteDownload"
const val TASK_SQLITE_CHECKSUM = "sqliteChecksum"
const val TASK_SQLITE_UNZIP = "sqliteUnzip"
const val TASK_SQLITE_COMPILE_SHARED = "sqliteCompileShared"
const val TASK_SQLITE_COMPILE_STATIC = "sqliteCompileStatic"
const val TASK_SQLITE_GENERATE_CINTEROP_DEF = "sqliteGenerateCInteropDef"

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

    val amalgamationFileName = extension.sqliteCompilationParameters.map {
        "sqlite3mc-${it.sqliteMCVersion}-sqlite-${it.sqliteVersion}-amalgamation.zip"
    }

    src(extension.sqliteCompilationParameters.zip(amalgamationFileName) { release, fileName ->
        "https://github.com/utelle/SQLite3MultipleCiphers/releases/download/" +
                "v${release.sqliteMCVersion}/$fileName"
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

    // Implicit dependency
    inputs.file(amalgamationFile)
    inputs.property("checksum", extension.sqliteDownloadChecksum)

    src(amalgamationFile)
    algorithm("SHA-256")

    // Unfortunately, verify task do not accept provider
    checksum(extension.sqliteDownloadChecksum.get())
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
 * Registers and returns the task responsible for generating the artifacts for static targets.
 */
fun Project.registerSqliteCompileStaticTask(): TaskProvider<SqliteCompileStaticTask> {
    val extension = sqliteCompilerExtension

    return tasks.register<SqliteCompileStaticTask>(TASK_SQLITE_COMPILE_STATIC) {
        group = sqliteCompilerTaskGroup

        // Explicit dependency on the unzip task
        dependsOn(rootProject.tasks.named(TASK_SQLITE_UNZIP))
        compilationParameters = extension.sqliteCompilationParameters
        sqliteSourcesDirectory = extension.sqliteSourcesDirectory
    }
}

///////////////////////////////////////////////////////////////////////////
// Interop tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the cinterop definition file for `this`
 * [KotlinNativeTarget].
 */
fun KotlinNativeTarget.registerSqliteGenerateCInteropDefTask(
    packageName: String,
    staticTarget: SqliteStaticTarget,
    defFileProvider: Provider<RegularFile>
): TaskProvider<Task> = project.tasks.register(
    name = "$TASK_SQLITE_GENERATE_CINTEROP_DEF${name.uppercaseFirstChar()}"
) {
    group = sqliteCompilerTaskGroup

    // Explicit dependency on the unzip task
    dependsOn(project.rootProject.tasks.named(TASK_SQLITE_UNZIP))
    outputs.file(defFileProvider)

    val parameters = project.sqliteCompilerExtension.sqliteCompilationParameters
    val libraryFile = staticTarget.libraryFile(parameters)

    doLast {
        defFileProvider.get().asFile.writeText(
            createDefContent(
                packageName = packageName,
                libraryFile = libraryFile.get().asFile,
                params = parameters.get()
            )
        )
    }
}