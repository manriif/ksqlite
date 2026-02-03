package tasks

import KsqliteChecksums
import KsqliteCompilerExtension
import androidToolchain
import compilation.SqliteTarget
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.VerifyAction
import interop.createDefContent
import interop.createSqliteCMakeListsContent
import interop.createSqliteFfmRuntimeMetadataContent
import interop.createSqliteJniRuntimeMetadataContent
import jextract.jextract
import jextract.jextractDownloadUrl
import jextract.jextractExtract
import jextract.jextractGenerateBindings
import jextractInstallTaskProvider
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
import platform.Platform
import sqliteHeaderFile
import sqliteInstallTaskProvider
import sqliteSourceFile
import toolchainDirectory
import toolchains.androidNdk
import toolchains.androidNdkDownloadUrl
import toolchains.androidNdkExtract
import utils.copyFirstDirectoryContent

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val ksqliteCompilerTaskGroup = "ksqlite"

const val TASK_TOOLCHAIN_ANDROID_DOWNLOAD = "toolchainAndroidDownload"
const val TASK_TOOLCHAIN_ANDROID_EXTRACT = "toolchainAndroidExtract"
const val TASK_SQLITE_DOWNLOAD = "sqliteDownload"
const val TASK_SQLITE_EXTRACT = "sqliteExtract"
const val TASK_SQLITEMC_DOWNLOAD = "sqliteMcDownload"
const val TASK_SQLITEMC_EXTRACT = "sqliteMcExtract"
const val TASK_SQLITE_INSTALL = "sqliteInstall"
const val TASK_JEXTRACT_DOWNLOAD = "jextractDownload"
const val TASK_JEXTRACT_EXTRACT = "jextractExtract"
const val TASK_JEXTRACT_GENERATE_BINDINGS = "jextractGenerateBindings"
const val TASK_SQLITE_COMPILE_SHARED = "sqliteCompileShared"
const val TASK_SQLITE_COMPILE_STATIC = "sqliteCompileStatic"
const val TASK_SQLITE_GENERATE_CINTEROP_DEF = "sqliteGenerateCInteropDef"
const val TASK_SQLITE_GENERATE_CMAKE_LISTS = "sqliteGenerateCMakeLists"
const val TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA = "sqliteGenerateJniRuntimeMetadata"
const val TASK_SQLITE_GENERATE_FFM_RUNTIME_METADATA = "sqliteGenerateFfmRuntimeMetadata"

///////////////////////////////////////////////////////////////////////////
// Root tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers the task for downloading sqlite sources.
 */
fun Project.registerTasks(extension: KsqliteCompilerExtension) {
    registerToolchainAndroidExtractTask(
        extension = extension,
        downloadTaskProvider = registerToolchainAndroidDownloadTask(extension)
    )

    registerJextractExtractTask(
        extension = extension,
        downloadTaskProvider = registerJextractDownloadTask(extension)
    )

    registerSqliteInstallTask(
        extension = extension,
        sqliteExtractTaskProvider = registerSqliteExtractTask(
            extension = extension,
            downloadTaskProvider = registerSqliteDownloadTask(extension)
        ),
        sqliteMcExtractTaskProvider = registerSqliteMcExtractTask(
            extension = extension,
            downloadTaskProvider = registerSqliteMcDownloadTask(extension)
        )
    )
}

/**
 * Registers and returns a task responsible for downloading and verifying a file.
 */
private fun Project.registerDownloadTask(
    name: String,
    extension: KsqliteCompilerExtension,
    url: Provider<String>,
    configureVerify: VerifyAction.(KsqliteChecksums) -> Unit,
): TaskProvider<Download> = tasks.register<Download>(name) {
    group = ksqliteCompilerTaskGroup

    val fileName = url.map { it.substringAfterLast('/') }

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
// SQLite
///////////////////////////////////////////////////////////////////////////

private fun String.pad2() = padStart(2, '0')

/**
 * Registers and returns the task responsible for downloading SQLite sources.
 */
private fun Project.registerSqliteDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_SQLITE_DOWNLOAD,
    extension = extension,
    url = extension.compilationParams.zip(extension.checksums) { params, checksums ->
        val components = params.sqliteVersion.split('.')
        val (major, minor, patch) = components
        val build = components.getOrElse(3) { "0" }
        val releaseYear = checksums.sqlite.split('.').first()
        val normalizedVersion = "$major${minor.pad2()}${patch.pad2()}${build.pad2()}"

        "https://www.sqlite.org/$releaseYear/sqlite-src-$normalizedVersion.zip"
    },
    configureVerify = { checksums ->
        algorithm("SHA3-256")
        checksum(checksums.sqlite.split('.').last())
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
    outputDirectory = downloadTaskProvider.zip(extension.downloadDirectory) { task, directory ->
        directory.dir(task.dest.nameWithoutExtension)
    },
    configure = { fileOperations, outputDirectory, downloadedFile ->
        val sources = zipTree(downloadedFile)

        doLast {
            fileOperations.copyFirstDirectoryContent(sources, outputDirectory)
        }
    }
)

///////////////////////////////////////////////////////////////////////////
// SQLite Multiple Ciphers
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading SQLite MC sources.
 */
private fun Project.registerSqliteMcDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_SQLITEMC_DOWNLOAD,
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
 * Registers and returns the task responsible for extracting the downloaded SQLite MC sources.
 */
private fun Project.registerSqliteMcExtractTask(
    extension: KsqliteCompilerExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_SQLITEMC_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = downloadTaskProvider.zip(extension.downloadDirectory) { task, directory ->
        directory.dir(task.dest.nameWithoutExtension)
    },
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
// SQLite install
///////////////////////////////////////////////////////////////////////////

/**
 * Registers the task responsible for installing sqlite (merging sources).
 */
private fun Project.registerSqliteInstallTask(
    extension: KsqliteCompilerExtension,
    sqliteExtractTaskProvider: TaskProvider<Task>,
    sqliteMcExtractTaskProvider: TaskProvider<Task>,
): TaskProvider<Task> = tasks.register(TASK_SQLITE_INSTALL) {
    group = ksqliteCompilerTaskGroup

    val destination = extension.sqliteSourcesDirectory
    val sqliteDirectory = sqliteExtractTaskProvider.map { it.outputs.files.singleFile }
    val sqliteMcDirectory = sqliteMcExtractTaskProvider.map { it.outputs.files.singleFile }
    val sqliteJniSources = fileTree(sqliteDirectory.map { it.resolve("ext/jni") })
    val sqliteJniDestination = destination.map { it.dir("jni") }
    val sqliteWasmSources = fileTree(sqliteDirectory.map { it.resolve("ext/wasm") })
    val sqliteWasmDestination = destination.map { it.dir("wasm") }
    val fileOperations = serviceOf<FileSystemOperations>()

    inputs.dir(sqliteDirectory)
    inputs.dir(sqliteMcDirectory)
    outputs.dir(destination)

    doFirst {
        fileOperations.delete {
            delete(destination)
        }
    }

    doLast {
        val params = extension.compilationParams.get()
        val jniDirectory = sqliteJniDestination.get().asFile
        val wasmDirectory = sqliteWasmDestination.get().asFile

        fileOperations.copy {
            from(sqliteMcDirectory, sqliteJniSources, sqliteWasmSources)
            include { it.name.startsWith(params.sqliteMcName) }
            into(destination)
        }

        /*fileOperations.copy {
            from(sqliteJniSources)
            into(jniDirectory)
        }

        fileOperations.copy {
            from( sqliteWasmSources)
            into(wasmDirectory)
        }*/
    }
}

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
 * The returned task depends on SQLite and Jextract installations.
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
    dependsOn(sqliteInstallTaskProvider)
    dependsOn(jextractInstallTaskProvider)

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
    dependsOn(project.sqliteInstallTaskProvider)

    val extension = project.ksqliteCompilerExtension
    compilationParameters = extension.compilationParams
    sqliteSourcesDirectory = extension.sqliteSourcesDirectory
}

/**
 * Registers and returns a task responsible for generating the artifacts for dynamic linking.
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteCompileSharedTask(
    name: String,
    configure: (SqliteCompileSharedTask.() -> Unit)? = null
): TaskProvider<SqliteCompileSharedTask> {
    return tasks.register<SqliteCompileSharedTask>("$TASK_SQLITE_COMPILE_SHARED$name") {
        configureCompileTask()
        configure?.invoke(this)
    }
}

/**
 * Registers and returns a task responsible for generating the artifacts for static linking.
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteCompileStaticTask(
    name: String,
    configure: (SqliteCompileStaticTask.() -> Unit)? = null
): TaskProvider<SqliteCompileStaticTask> {
    return tasks.register<SqliteCompileStaticTask>("$TASK_SQLITE_COMPILE_STATIC$name") {
        configureCompileTask()
        configure?.invoke(this)
    }
}

///////////////////////////////////////////////////////////////////////////
// Interop
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the cinterop definition file for
 * [target].
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteGenerateCInteropDefTask(
    packageName: String,
    target: SqliteTarget,
    defFileProvider: Provider<RegularFile>
): TaskProvider<Task> = project.tasks.register(
    name = "$TASK_SQLITE_GENERATE_CINTEROP_DEF${name.uppercaseFirstChar()}"
) {
    group = ksqliteCompilerTaskGroup

    // Explicit dependency on the sqlite install task
    dependsOn(sqliteInstallTaskProvider)
    outputs.file(defFileProvider)

    val extension = ksqliteCompilerExtension

    doLast {
        defFileProvider.get().asFile.writeText(
            createDefContent(
                packageName = packageName,
                libraryFile = target.libraryFile.get().asFile,
                params = extension.compilationParams.get()
            )
        )
    }
}

/**
 * Registers and returns the task responsible for generating the CMakeList.txt file for SQLite.
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteGenerateCMakeListsTask(
    cmakeListsFile: Provider<RegularFile>,
    cmakeVersion: String
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_CMAKE_LISTS) {
    group = ksqliteCompilerTaskGroup

    // Explicit dependency on sqlite install task
    dependsOn(sqliteInstallTaskProvider)

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
    metadataFile: Provider<RegularFile>,
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA) {
    group = ksqliteCompilerTaskGroup
    outputs.file(metadataFile)

    val extension = ksqliteCompilerExtension

    doLast {
        metadataFile.get().asFile.writeText(
            createSqliteJniRuntimeMetadataContent(
                packageName = packageName,
                libraryName = extension.compilationParams.get().libraryName
            )
        )
    }
}

/**
 * Registers and returns the task responsible for generating FFM runtime metadata for SQLite.
 */
fun Project.registerSqliteFfmRuntimeMetadataTask(
    packageName: String,
    nativeDirectoryName: String,
    metadataFile: Provider<RegularFile>,
    platforms: Provider<List<Platform>>
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_FFM_RUNTIME_METADATA) {
    group = ksqliteCompilerTaskGroup
    outputs.file(metadataFile)

    val extension = ksqliteCompilerExtension

    doLast {
        metadataFile.get().asFile.writeText(
            createSqliteFfmRuntimeMetadataContent(
                packageName = packageName,
                nativeDirectoryName = nativeDirectoryName,
                libraryName = extension.compilationParams.get().libraryName,
                platforms = platforms.get()
            )
        )
    }
}