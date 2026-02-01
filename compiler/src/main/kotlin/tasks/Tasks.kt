package tasks

import KsqliteChecksums
import KsqliteCompilerExtension
import androidToolchain
import compilation.SqliteTarget
import compilation.staticLibraryFile
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.VerifyAction
import interop.createDefContent
import interop.createSqliteCMakeListsContent
import interop.createSqliteJniRuntimeMetadataContent
import jextract.jextract
import jextract.jextractDownloadUrl
import jextract.jextractExtract
import jextract.jextractGenerateBindings
import ksqliteCompilerExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.process.ExecOperations
import sqliteDynamicLibraryName
import sqliteHeaderFile
import sqliteSourceFile
import toolchainDirectory
import toolchains.androidNdk
import toolchains.androidNdkDownloadUrl
import toolchains.androidNdkExtract

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val ksqliteCompilerTaskGroup = "ksqlite"

const val TASK_INSTALL_AND_CONFIGURE = "installAndConfigure"
const val TASK_TOOLCHAIN_ANDROID_DOWNLOAD = "toolchainAndroidDownload"
const val TASK_TOOLCHAIN_ANDROID_EXTRACT = "toolchainAndroidExtract"
const val TASK_SQLITE_DOWNLOAD = "sqliteDownload"
const val TASK_SQLITE_EXTRACT = "sqliteExtract"
const val TASK_JEXTRACT_DOWNLOAD = "jextractDownload"
const val TASK_JEXTRACT_EXTRACT = "jextractExtract"
const val TASK_JEXTRACT_GENERATE_BINDINGS = "jextractGenerateBindings"
const val TASK_SQLITE_COMPILE_SHARED = "sqliteCompileShared"
const val TASK_SQLITE_COMPILE_STATIC = "sqliteCompileStatic"
const val TASK_SQLITE_GENERATE_CINTEROP_DEF = "sqliteGenerateCInteropDef"
const val TASK_SQLITE_GENERATE_CMAKE_LISTS = "sqliteGenerateCMakeLists"
const val TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA = "sqliteGenerateJniRuntimeMetadata"

///////////////////////////////////////////////////////////////////////////
// Root tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers the task for downloading sqlite sources.
 */
fun Project.registerTasks(extension: KsqliteCompilerExtension) {
    val androidTaskProvider = registerToolchainAndroidExtractTask(
        extension = extension,
        downloadTaskProvider = registerToolchainAndroidDownloadTask(extension)
    )

    val sqliteTaskProvider = registerSqliteExtractTask(
        extension = extension,
        downloadTaskProvider = registerSqliteDownloadTask(extension)
    )

    val jextractTaskProvider = registerJextractExtractTask(
        extension = extension,
        downloadTaskProvider = registerJextractDownloadTask(extension)
    )

    // Global task to wait on all toolchains and sqlite extraction
    tasks.register(TASK_INSTALL_AND_CONFIGURE) {
        dependsOn(androidTaskProvider)
        dependsOn(sqliteTaskProvider)
        dependsOn(jextractTaskProvider)
    }
}

/**
 * Registers and returns a task responsible for downloading and verifying a file.
 */
private fun Project.registerDownloadTask(
    name: String,
    extension: KsqliteCompilerExtension,
    url: Provider<String>,
    fileName: Provider<String> = url.map { it.substringAfterLast('/') },
    configureVerify: VerifyAction.(KsqliteChecksums) -> Unit,
    configureDownload: (Download.(fileName: Provider<String>) -> Unit)? = null
): TaskProvider<Download> = tasks.register<Download>(name) {
    group = ksqliteCompilerTaskGroup

    val destination = extension.downloadDirectory.zip(fileName) { directory, fileName ->
        directory.file(fileName)
    }

    // Skip if the host is not-supported
    onlyIf { url.isPresent }
    inputs.property("url", url)

    dest(destination)
    src(url)
    overwrite(false)
    quiet(false)
    configureDownload?.invoke(this, fileName)

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
private inline fun Project.registerExtractTask(
    name: String,
    downloadTaskProvider: TaskProvider<Download>,
    outputDirectory: Provider<Directory>,
    crossinline configure: Task.(
        fileOperation: FileSystemOperations,
        outputDirectory: Provider<Directory>,
        file: Provider<RegularFile>,
    ) -> Unit
): TaskProvider<Task> = tasks.register(name) {
    group = ksqliteCompilerTaskGroup

    val fileOperations = serviceOf<FileSystemOperations>()
    val downloadedFile = layout.file(downloadTaskProvider.map { it.dest })

    inputs.file(downloadedFile)
    outputs.dir(outputDirectory)

    configure(fileOperations, outputDirectory, downloadedFile)

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
    url = extension.androidToolchain().map { androidNdkDownloadUrl(it.version) },
    configureVerify = { checksums ->
        algorithm("SHA-1")
        checksum(checksums.androidNdk())
    }
)

/**
 * Registers and returns the task responsible for extracting the Android NDK.
 */
private fun Project.registerToolchainAndroidExtractTask(
    extension: KsqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_TOOLCHAIN_ANDROID_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = layout.toolchainDirectory(extension.androidToolchain()),
    configure = { fileOperations, outputDirectory, downloadedFile ->
        androidNdkExtract(
            version = extension.androidToolchain().map { it.version },
            fileOperations = fileOperations,
            downloadedFile = downloadedFile,
            destination = outputDirectory
        )
    }
)

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
    url = extension.compilationParams.map { params ->
        "https://github.com/utelle/SQLite3MultipleCiphers/releases/download/" +
                "v${params.sqliteMCVersion}/sqlite3mc-${params.sqliteMCVersion}-sqlite-" +
                "${params.sqliteVersion}-amalgamation.zip"
    },
    configureVerify = { checksums ->
        algorithm("SHA-256")
        checksum(checksums.sqliteMc)
    }
)

/**
 * Registers and returns the task responsible for extracting the downloaded SQLite sources.
 */
private fun Project.registerSqliteExtractTask(
    extension: KsqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_SQLITE_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = extension.sqliteSourcesDirectory,
    configure = { fileOperations, outputDirectory, downloadedFile ->
        val sources = zipTree(downloadedFile)

        doLast {
            fileOperations.copy {
                from(sources)
                into(outputDirectory)
            }
        }
    }
)

///////////////////////////////////////////////////////////////////////////
// Jextract
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading Jextract.
 */
private fun Project.registerJextractDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_JEXTRACT_DOWNLOAD,
    extension = extension,
    url = extension.jdkVersion.zip(extension.jExtractVersion) { jdkVersion, jextractVersion ->
        jextractDownloadUrl(jdkVersion = jdkVersion, jextractVersion = jextractVersion)
    },
    configureVerify = { checksums ->
        algorithm("SHA-256")
        checksum(checksums.jextract())
    }
)

/**
 * Registers and returns the task responsible for extracting the downloaded Jextract.
 */
private fun Project.registerJextractExtractTask(
    extension: KsqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_JEXTRACT_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = extension.jExtractDirectory,
    configure = { fileOperations, outputDirectory, downloadedFile ->
        jextractExtract(fileOperations, downloadedFile, outputDirectory)
    }
)

/**
 * Registers and returns the task responsible for generating SQLite bindings using Jextract.
 */
fun Project.registerJextractGenerateBindingsTask(
    packageName: String,
    outputDirectory: Provider<Directory>
): TaskProvider<Task> = project.tasks.register(TASK_JEXTRACT_GENERATE_BINDINGS) {
    group = ksqliteCompilerTaskGroup

    val fileOperations = serviceOf<FileSystemOperations>()
    val execOperations = serviceOf<ExecOperations>()
    val extension = ksqliteCompilerExtension

    val headerFile = sqliteHeaderFile(
        sources = extension.sqliteSourcesDirectory,
        params = extension.compilationParams
    )

    // Explicit dependency on sqlite and jextract extract tasks
    dependsOn(project.rootProject.tasks.named(TASK_JEXTRACT_EXTRACT))
    dependsOn(project.rootProject.tasks.named(TASK_SQLITE_EXTRACT))

    inputs.file(headerFile)
    outputs.dir(outputDirectory)

    doFirst {
        fileOperations.delete {
            delete(outputDirectory)
        }

        outputDirectory.get().asFile.mkdirs()
    }

    doLast {
        jextractGenerateBindings(
            execOperations = execOperations,
            packageName = packageName,
            libraryName = sqliteDynamicLibraryName(extension.compilationParams).get(),
            jextractDirectory = extension.jExtractDirectory.get().asFile,
            sqliteHeaderFile = headerFile.get().asFile,
            outputDirectory = outputDirectory.get().asFile,
            params = extension.compilationParams.get()
        )
    }
}

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the artifacts for dynamic linking.
 */
private fun SqliteCompileTask.configureCompileTask() {
    group = ksqliteCompilerTaskGroup

    // Depend on toolchains and sqlite installation
    dependsOn(project.rootProject.tasks.named(TASK_INSTALL_AND_CONFIGURE))

    val extension = project.ksqliteCompilerExtension
    compilationParameters = extension.compilationParams
    sqliteSourcesDirectory = extension.sqliteSourcesDirectory
}

/**
 * Registers and returns the task responsible for generating the artifacts for dynamic linking.
 */
fun Project.registerSqliteCompileSharedTask(): TaskProvider<SqliteCompileSharedTask> {
    return tasks.register<SqliteCompileSharedTask>(TASK_SQLITE_COMPILE_SHARED) {
        configureCompileTask()
    }
}

/**
 * Registers and returns the task responsible for generating the artifacts for static linking.
 */
fun Project.registerSqliteCompileStaticTask(): TaskProvider<SqliteCompileStaticTask> {
    return tasks.register<SqliteCompileStaticTask>(TASK_SQLITE_COMPILE_STATIC) {
        configureCompileTask()
    }
}

///////////////////////////////////////////////////////////////////////////
// Interop
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the cinterop definition file for
 * [target].
 */
fun Project.registerSqliteGenerateCInteropDefTask(
    packageName: String,
    target: SqliteTarget,
    defFileProvider: Provider<RegularFile>
): TaskProvider<Task> = project.tasks.register(
    name = "$TASK_SQLITE_GENERATE_CINTEROP_DEF${name.uppercaseFirstChar()}"
) {
    group = ksqliteCompilerTaskGroup

    // Explicit dependency on the global install and configure task
    dependsOn(project.rootProject.tasks.named(TASK_INSTALL_AND_CONFIGURE))
    outputs.file(defFileProvider)

    val parameters = project.ksqliteCompilerExtension.compilationParams
    val libraryFile = target.staticLibraryFile(parameters)

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

/**
 * Registers and returns the task responsible for generating the CMakeList.txt file for SQLite.
 */
fun Project.registerSqliteGenerateCMakeListsTask(
    cmakeListsFile: Provider<RegularFile>,
    cmakeVersion: String
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_CMAKE_LISTS) {
    group = ksqliteCompilerTaskGroup

    // Explicit dependency on sqlite extract task
    dependsOn(project.rootProject.tasks.named(TASK_SQLITE_EXTRACT))

    val extension = ksqliteCompilerExtension

    val headerFile = sqliteHeaderFile(
        sources = extension.sqliteSourcesDirectory,
        params = extension.compilationParams
    )

    val sourceFile = sqliteSourceFile(
        sources = extension.sqliteSourcesDirectory,
        params = extension.compilationParams
    )

    inputs.files(headerFile, sourceFile)
    outputs.file(cmakeListsFile)

    doLast {
        cmakeListsFile.get().asFile.writeText(
            createSqliteCMakeListsContent(
                cmakeVersion = cmakeVersion,
                sqliteHeaderFile = headerFile.get().asFile,
                sqliteSourceFile = sourceFile.get().asFile,
                params = extension.compilationParams.get()
            )
        )
    }
}

/**
 * Registers and returns the task responsible for generating JNI runtime metadata for SQLite.
 */
fun Project.registerSqliteJniRuntimeMetadataTask(
    packageName: String,
    libraryName: String,
    metadataFile: Provider<RegularFile>,
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA) {
    group = ksqliteCompilerTaskGroup

    // Explicit dependency on sqlite extract task
    dependsOn(project.rootProject.tasks.named(TASK_SQLITE_EXTRACT))
    outputs.file(metadataFile)

    doLast {
        metadataFile.get().asFile.writeText(
            createSqliteJniRuntimeMetadataContent(
                packageName = packageName,
                libraryName = libraryName
            )
        )
    }
}