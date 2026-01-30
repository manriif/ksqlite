package tasks

import KsqliteChecksums
import KsqliteCompilerExtension
import compilation.SqliteStaticTarget
import compilation.libraryFile
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.VerifyAction
import interop.createDefContent
import ksqliteCompilerExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import toolchains.androidNdk
import toolchains.androidNdkDirectory
import toolchains.androidNdkDownloadFileName
import toolchains.androidNdkExtract
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val ksqliteCompilerTaskGroup = "ksqlite"

const val TASK_TOOLCHAIN_ANDROID_DOWNLOAD = "toolchainAndroidDownload"
const val TASK_TOOLCHAIN_ANDROID_EXTRACT = "toolchainAndroidExtract"
const val TASK_SQLITE_DOWNLOAD = "sqliteDownload"
const val TASK_SQLITE_EXTRACT = "sqliteExtract"
const val TASK_SQLITE_COMPILE_DYNAMIC = "sqliteCompileDynamic"
const val TASK_SQLITE_COMPILE_STATIC = "sqliteCompileStatic"
const val TASK_SQLITE_GENERATE_CINTEROP_DEF = "sqliteGenerateCInteropDef"

///////////////////////////////////////////////////////////////////////////
// Root tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers the task for downloading sqlite sources.
 */
fun Project.registerTasks(extension: KsqliteCompilerExtension) {
    registerToolchainAndroidExtractTask(extension, registerToolchainAndroidDownloadTask(extension))
    registerSqliteExtractTask(extension, registerSqliteDownloadTask(extension))
}

/**
 * Registers and returns a task responsible for downloading and verifying a file.
 */
private fun Project.registerDownloadTask(
    name: String,
    extension: KsqliteCompilerExtension,
    fileName: Provider<String>,
    configureVerify: VerifyAction.(KsqliteChecksums) -> Unit,
    configureDownload: Download.(fileName: Provider<String>) -> Unit
): TaskProvider<Download> = tasks.register<Download>(name) {
    group = ksqliteCompilerTaskGroup

    val destination = extension.downloadDirectory.zip(fileName) { directory, fileName ->
        directory.file(fileName)
    }

    dest(destination)
    overwrite(false)
    quiet(false)
    configureDownload(fileName)

    val verify = VerifyAction(layout).apply {
        src(destination)
    }

    doLast {
        configureVerify(verify, extension.checksums.get())
        verify.execute()
    }
}

/**
 * Registers and returns a task responsible for extracting a file.
 */
private fun Project.registerExtractTask(
    name: String,
    downloadTaskProvider: TaskProvider<Download>,
    outputDirectory: Provider<Directory>,
    configure: Task.(file: Provider<File>) -> FileTree
): TaskProvider<Task> = tasks.register(name) {
    group = ksqliteCompilerTaskGroup

    val fileOperations = serviceOf<FileSystemOperations>()
    val downloadedFile = downloadTaskProvider.map { it.dest }

    inputs.file(downloadedFile)
    outputs.dir(outputDirectory)

    val sources = configure(downloadedFile)

    // Adds the copy action before the last actions which can be a cleanup added in configure
    actions.add(actions.lastIndex) {
        fileOperations.copy {
            from(sources)
            into(outputDirectory)
        }
    }

    doFirst {
        fileOperations.delete {
            delete(outputDirectory)
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Android NDK
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading the Android NDK.
 */
private fun Project.registerToolchainAndroidDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_TOOLCHAIN_ANDROID_DOWNLOAD,
    extension = extension,
    fileName = extension.toolchainVersions.map { androidNdkDownloadFileName(it.android) },
    configureVerify = { checksums ->
        algorithm("SHA-1")
        checksum(checksums.androidNdk())
    },
    configureDownload = { ndkFileName ->
        src(ndkFileName.map { "https://dl.google.com/android/repository/$it" })
    }
)

/**
 * Registers and returns the task responsible for extracting the Android NDK.
 */
@Suppress("NewApi")
private fun Project.registerToolchainAndroidExtractTask(
    extension: KsqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_TOOLCHAIN_ANDROID_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = androidNdkDirectory(extension.toolchainsDirectory),
) { downloadedFile ->
    androidNdkExtract(
        version = extension.toolchainVersions.map { it.android },
        downloadedFile = downloadedFile
    )
}

///////////////////////////////////////////////////////////////////////////
// Sqlite Multiple Ciphers
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading SQLite sources.
 */
private fun Project.registerSqliteDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_SQLITE_DOWNLOAD,
    extension = extension,
    fileName = extension.sqliteCompilationParameters.map { params ->
        "sqlite3mc-${params.sqliteMCVersion}-sqlite-${params.sqliteVersion}-amalgamation.zip"
    },
    configureVerify = { checksums ->
        algorithm("SHA-256")
        checksum(checksums.sqliteMultipleCiphers)
    },
    configureDownload = { fileName ->
        src(extension.sqliteCompilationParameters.zip(fileName) { params, fileName ->
            "https://github.com/utelle/SQLite3MultipleCiphers/releases/download/" +
                    "v${params.sqliteMCVersion}/$fileName"
        })
    }
)

/**
 * Registers and returns the task responsible for extracting the downloaded SQLite sources.
 */
@Suppress("NewApi")
private fun Project.registerSqliteExtractTask(
    extension: KsqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_SQLITE_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = extension.sqliteSourcesDirectory,
    configure = { zipTree(it) }
)

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the artifacts for static targets.
 */
fun Project.registerSqliteCompileStaticTask(): TaskProvider<SqliteCompileStaticTask> {
    val extension = ksqliteCompilerExtension

    return tasks.register<SqliteCompileStaticTask>(TASK_SQLITE_COMPILE_STATIC) {
        group = ksqliteCompilerTaskGroup

        // Explicit dependency on the unzip task
        dependsOn(rootProject.tasks.named(TASK_SQLITE_EXTRACT))
        compilationParameters = extension.sqliteCompilationParameters
        sqliteSourcesDirectory = extension.sqliteSourcesDirectory
    }
}

///////////////////////////////////////////////////////////////////////////
// Interop
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
    group = ksqliteCompilerTaskGroup

    // Explicit dependency on the unzip task
    dependsOn(project.rootProject.tasks.named(TASK_SQLITE_EXTRACT))
    outputs.file(defFileProvider)

    val parameters = project.ksqliteCompilerExtension.sqliteCompilationParameters
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