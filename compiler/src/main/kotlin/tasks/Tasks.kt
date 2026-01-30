package tasks

import KsqliteCompilerExtension
import compilation.SqliteStaticTarget
import compilation.libraryFile
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.VerifyAction
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
import ksqliteCompilerExtension

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
    registerSqliteUnzipTask(extension, registerSqliteDownloadTask(extension))
}

///////////////////////////////////////////////////////////////////////////
// Android NDK
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading the necessary toolchains.
 */
private fun Project.registerToolchainAndroidDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = tasks.register<Download>(TASK_TOOLCHAIN_ANDROID_DOWNLOAD) {
    group = ksqliteCompilerTaskGroup

}

///////////////////////////////////////////////////////////////////////////
// Sqlite
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading SQLite sources.
 */
private fun Project.registerSqliteDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = tasks.register<Download>(TASK_SQLITE_DOWNLOAD) {
    group = ksqliteCompilerTaskGroup

    val amalgamationFileName = extension.sqliteCompilationParameters.map {
        "sqlite3mc-${it.sqliteMCVersion}-sqlite-${it.sqliteVersion}-amalgamation.zip"
    }

    src(extension.sqliteCompilationParameters.zip(amalgamationFileName) { params, file ->
        "https://github.com/utelle/SQLite3MultipleCiphers/releases/download/" +
                "v${params.sqliteMCVersion}/$file"
    })

    val destination = extension.sqliteDownloadDirectory.zip(amalgamationFileName) { dir, file ->
        dir.file(file)
    }

    dest(destination)
    overwrite(false)

    val verify = VerifyAction(layout).apply {
        src(destination)
        algorithm("SHA-256")
    }

    doLast {
        verify.checksum(extension.checksums.get().sqliteMultipleCiphers)
        verify.execute()
    }
}

/**
 * Registers and returns the task responsible for unzipping the downloaded SQLite sources.
 */
@Suppress("NewApi")
private fun Project.registerSqliteUnzipTask(
    extension: KsqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Copy> = tasks.register<Copy>(TASK_SQLITE_EXTRACT) {
    group = ksqliteCompilerTaskGroup

    val fileOperations = serviceOf<FileSystemOperations>()
    val amalgamationFile = downloadTaskProvider.map { it.dest }
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