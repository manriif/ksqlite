package tasks

import KsqliteChecksums
import KsqliteExtension
import androidToolchain
import compilation.SqliteTarget
import compilation.compileSqliteWasm
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.VerifyAction
import emscriptenInstallTaskProvider
import gnuSedInstallTaskProvider
import jextractInstallTaskProvider
import ksqliteExtension
import modules.adjustSqliteSourceTreeForWasmCompilation
import modules.createDefContent
import modules.createSqliteCMakeListsContent
import modules.createSqliteFfmRuntimeMetadataContent
import modules.createSqliteJniRuntimeMetadataContent
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
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
import toolDirectory
import tools.androidNdk
import tools.androidNdkDownloadUrl
import tools.androidNdkExtract
import tools.androidNdkInstall
import tools.emscriptenInstall
import tools.emsdkDownloadUrl
import tools.emsdkExtract
import tools.gnuSedDownloadUrl
import tools.gnuSedExtract
import tools.gnuSedInstall
import tools.jextract
import tools.jextractDownloadUrl
import tools.jextractExtract
import tools.jextractGenerateBindings
import tools.wabtDownloadUrl
import tools.wabtExtract
import tools.wabtInstall
import utils.copyFirstDirectoryContent
import utils.sha256
import wabtInstallTaskProvider
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val ksqliteTaskGroup = "ksqlite"

const val TASK_TOOLCHAIN_ANDROID_INSTALL = "toolchainAndroidInstall"
const val TASK_TOOLCHAIN_ANDROID_DOWNLOAD = "toolchainAndroidDownload"
const val TASK_TOOLCHAIN_ANDROID_EXTRACT = "toolchainAndroidExtract"
const val TASK_EMSCRIPTEN_INSTALL = "emscriptenInstall"
const val TASK_EMSCRIPTEN_DOWNLOAD = "emscriptenDownload"
const val TASK_EMSCRIPTEN_EXTRACT = "emscriptenExtract"
const val TASK_WABT_INSTALL = "wabtInstall"
const val TASK_WABT_DOWNLOAD = "wabtDownload"
const val TASK_WABT_EXTRACT = "wabtExtract"
const val TASK_GNU_SED_INSTALL = "gnuSedInstall"
const val TASK_GNU_SED_DOWNLOAD = "gnuSedDownload"
const val TASK_GNU_SED_EXTRACT = "gnuSedExtract"
const val TASK_JEXTRACT_DOWNLOAD = "jextractDownload"
const val TASK_JEXTRACT_EXTRACT = "jextractExtract"
const val TASK_JEXTRACT_GENERATE_BINDINGS = "jextractGenerateBindings"
const val TASK_SQLITE_INSTALL = "sqliteInstall"
const val TASK_SQLITE_DOWNLOAD = "sqliteDownload"
const val TASK_SQLITE_EXTRACT = "sqliteExtract"
const val TASK_SQLITEMC_DOWNLOAD = "sqliteMcDownload"
const val TASK_SQLITEMC_EXTRACT = "sqliteMcExtract"
const val TASK_SQLITE_COMPILE_SHARED = "sqliteCompileShared"
const val TASK_SQLITE_COMPILE_STATIC = "sqliteCompileStatic"
const val TASK_SQLITE_COMPILE_WASM = "sqliteCompileWasm"
const val TASK_SQLITE_GENERATE_CINTEROP_DEF = "sqliteGenerateCInteropDef"
const val TASK_SQLITE_GENERATE_CMAKE_LISTS = "sqliteGenerateCMakeLists"
const val TASK_SQLITE_COPY_JNI_JAVA_SOURCES = "sqliteCopyJniJavaSources"
const val TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA = "sqliteGenerateJniRuntimeMetadata"
const val TASK_SQLITE_GENERATE_FFM_RUNTIME_METADATA = "sqliteGenerateFfmRuntimeMetadata"

///////////////////////////////////////////////////////////////////////////
// Root project tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers the task for downloading sqlite sources.
 */
fun Project.registerRootTasks(extension: KsqliteExtension) {
    registerToolchainAndroidInstallTask(
        extension = extension,
        extractTaskProvider = registerToolchainAndroidExtractTask(
            extension = extension,
            downloadTaskProvider = registerToolchainAndroidDownloadTask(extension)
        )
    )

    registerEmscriptenInstallTask(
        extension = extension,
        extractTaskProvider = registerEmscriptenExtractTask(
            extension = extension,
            downloadTaskProvider = registerEmscriptenDownloadTask(extension)
        )
    )

    registerWabtInstallTask(
        extension = extension,
        extractTaskProvider = registerWabtExtractTask(
            extension = extension,
            downloadTaskProvider = registerWabtDownloadTask(extension)
        )
    )

    registerGnuSedInstallTask(
        extension = extension,
        extractTaskProvider = registerGnuSedExtractTask(
            extension = extension,
            downloadTaskProvider = registerGnuSedDownloadTask(extension)
        )
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

///////////////////////////////////////////////////////////////////////////
// Checksum
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a file that can be used to store a checksum for task generated output(s).
 */
fun Task.checksumFile(): RegularFile {
    return project.rootProject.layout.projectDirectory.file(
        ".ksqlite/${project.name}/$name/checksum.txt"
    )
}

/**
 * Invokes [execute] if [checksumFile] exists and its value differs from the one supplied by
 * [currentChecksum].
 */
inline fun executeIfChecksumChanged(
    checksumFile: RegularFile,
    currentChecksum: () -> String,
    execute: () -> Unit
) {
    val hashFile = checksumFile.asFile

    if (hashFile.exists()) {
        val current = currentChecksum()
        val previous = hashFile.readText()

        if (current == previous) {
            return
        }
    }

    execute()
    hashFile.parentFile.mkdirs()
    hashFile.writeText(currentChecksum())
}

///////////////////////////////////////////////////////////////////////////
// Download, Extract, Install
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a directory where to extract downloaded file in the download directory.
 */
private fun TaskProvider<Download>.extractDirectory(
    extension: KsqliteExtension
): Provider<Directory> = zip(extension.downloadDirectory) { task, directory ->
    directory.dir(task.dest.nameWithoutExtension)
}

/**
 * Returns a provider resolving the output directory of the task.
 */
private fun TaskProvider<Task>.outputDirectory(): Provider<File> {
    return map { it.outputs.files.singleFile }
}

/**
 * Registers and returns a task responsible for downloading and verifying a file.
 */
private fun Project.registerDownloadTask(
    name: String,
    extension: KsqliteExtension,
    url: Provider<String>,
    fileNamePrefix: String = "",
    configureVerify: VerifyAction.(KsqliteChecksums) -> Unit,
): TaskProvider<Download> = tasks.register<Download>(name) {
    group = ksqliteTaskGroup

    val fileName = url.map { "$fileNamePrefix${it.substringAfterLast('/')}" }

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
private fun Project.registerExtractTask(
    name: String,
    downloadTaskProvider: TaskProvider<Download>,
    outputDirectory: Provider<Directory>,
    configure: Task.(
        fileOperation: FileSystemOperations,
        outputDirectory: Provider<Directory>,
        file: Provider<RegularFile>,
    ) -> Unit
): TaskProvider<Task> = tasks.register(name) {
    group = ksqliteTaskGroup

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

/**
 * Registers and returns a task responsible for installing a tool.
 */
private fun Project.registerInstallTask(
    name: String,
    outputDirectoryProvider: Provider<Directory>,
    configureTask: Task.() -> Unit,
    install: (fileOperations: FileSystemOperations, outputDirectory: File) -> Unit
): TaskProvider<Task> = tasks.register(name) {
    group = ksqliteTaskGroup
    outputs.dir(outputDirectoryProvider)

    val checksumFile = checksumFile()
    val fileOperations = serviceOf<FileSystemOperations>()

    configureTask()

    doLast {
        val outputDirectory = outputDirectoryProvider.get().asFile

        executeIfChecksumChanged(checksumFile, outputDirectory::sha256) {
            fileOperations.delete { delete(outputDirectory) }
            install(fileOperations, outputDirectory)
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
    extension: KsqliteExtension
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
    extension: KsqliteExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_TOOLCHAIN_ANDROID_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = downloadTaskProvider.extractDirectory(extension),
    configure = { fileOperations, outputDirectory, downloadedFile ->
        androidNdkExtract(
            fileOperations = fileOperations,
            downloadedFile = downloadedFile,
            destination = outputDirectory
        )
    }
)

/**
 * Registers and returns the task responsible for installing the Android NDK.
 */
private fun Project.registerToolchainAndroidInstallTask(
    extension: KsqliteExtension,
    extractTaskProvider: TaskProvider<Task>
): TaskProvider<Task> {
    val tmpDirectory = extractTaskProvider.outputDirectory()
    val downloadedFile = extractTaskProvider.map { it.inputs.files.singleFile }
    val execOperations = project.serviceOf<ExecOperations>()
    val androidToolchain = extension.androidToolchain()

    return registerInstallTask(
        name = TASK_TOOLCHAIN_ANDROID_INSTALL,
        outputDirectoryProvider = layout.toolDirectory(androidToolchain),
        configureTask = {
            inputs.file(downloadedFile)
            inputs.dir(tmpDirectory)
        },
        install = { fileOperations, outputDirectory ->
            androidNdkInstall(
                version = androidToolchain.get().version,
                fileOperations = fileOperations,
                execOperations = execOperations,
                downloadedFile = downloadedFile.get(),
                inputDirectory = tmpDirectory.get(),
                outputDirectory = outputDirectory
            )
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// Emscripten
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading Emsdk.
 */
private fun Project.registerEmscriptenDownloadTask(
    extension: KsqliteExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_EMSCRIPTEN_DOWNLOAD,
    extension = extension,
    url = extension.tools.map { emsdkDownloadUrl(it.emsdk.version) },
    fileNamePrefix = "emsdk-",
    configureVerify = { checksums ->
        algorithm("SHA-256")
        checksum(checksums.emsdk)
    }
)

/**
 * Registers and returns the task responsible for extracting the downloaded Emsdk.
 */
private fun Project.registerEmscriptenExtractTask(
    extension: KsqliteExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_EMSCRIPTEN_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = downloadTaskProvider.extractDirectory(extension),
    configure = { fileOperations, outputDirectory, downloadedFile ->
        emsdkExtract(fileOperations, downloadedFile, outputDirectory)
    }
)

/**
 * Registers and returns the task responsible for installing emscripten from emsdk.
 */
fun Project.registerEmscriptenInstallTask(
    extension: KsqliteExtension,
    extractTaskProvider: TaskProvider<Task>,
): TaskProvider<Task> {
    val emsdkTmpDirectory = extractTaskProvider.outputDirectory()
    val execOperations = project.serviceOf<ExecOperations>()

    return registerInstallTask(
        name = TASK_EMSCRIPTEN_INSTALL,
        outputDirectoryProvider = layout.toolDirectory(extension.tools.map { it.emsdk }),
        configureTask = { inputs.dir(emsdkTmpDirectory) },
        install = { fileOperations, outputDirectory ->
            emscriptenInstall(
                fileOperations = fileOperations,
                execOperations = execOperations,
                inputDirectory = emsdkTmpDirectory.get(),
                outputDirectory = outputDirectory
            )
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// WABT
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading Wabt.
 */
private fun Project.registerWabtDownloadTask(
    extension: KsqliteExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_WABT_DOWNLOAD,
    extension = extension,
    url = extension.tools.map { wabtDownloadUrl(it.wabt.version) },
    configureVerify = { checksums ->
        algorithm("SHA-256")
        checksum(checksums.wabt)
    }
)

/**
 * Registers and returns the task responsible for extracting the downloaded Wabt.
 */
private fun Project.registerWabtExtractTask(
    extension: KsqliteExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_WABT_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = downloadTaskProvider.extractDirectory(extension),
    configure = { _, outputDirectory, downloadedFile ->
        wabtExtract(downloadedFile, outputDirectory)
    }
)

/**
 * Registers and returns the task responsible for installing wabt.
 */
fun Project.registerWabtInstallTask(
    extension: KsqliteExtension,
    extractTaskProvider: TaskProvider<Task>,
): TaskProvider<Task> {
    val wabtTmpDirectory = extractTaskProvider.outputDirectory()
    val execOperations = project.serviceOf<ExecOperations>()

    return registerInstallTask(
        name = TASK_WABT_INSTALL,
        outputDirectoryProvider = layout.toolDirectory(extension.tools.map { it.wabt }),
        configureTask = { inputs.dir(wabtTmpDirectory) },
        install = { fileOperations, outputDirectory ->
            wabtInstall(
                fileOperations = fileOperations,
                execOperations = execOperations,
                inputDirectory = wabtTmpDirectory.get(),
                outputDirectory = outputDirectory
            )
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// GNU sed
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading GNU sed.
 */
private fun Project.registerGnuSedDownloadTask(
    extension: KsqliteExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_GNU_SED_DOWNLOAD,
    extension = extension,
    url = extension.tools.map { gnuSedDownloadUrl(it.gnuSed.version) },
    configureVerify = { checksums ->
        algorithm("SHA-256")
        checksum(checksums.gnuSed)
    }
)

/**
 * Registers and returns the task responsible for extracting the downloaded GNU sed.
 */
private fun Project.registerGnuSedExtractTask(
    extension: KsqliteExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_GNU_SED_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = downloadTaskProvider.extractDirectory(extension),
    configure = { fileOperations, outputDirectory, downloadedFile ->
        gnuSedExtract(fileOperations, downloadedFile, outputDirectory)
    }
)

/**
 * Registers and returns the task responsible for installing GNU sed.
 */
fun Project.registerGnuSedInstallTask(
    extension: KsqliteExtension,
    extractTaskProvider: TaskProvider<Task>,
): TaskProvider<Task> {
    val gnuSedTmpDirectory = extractTaskProvider.outputDirectory()
    val execOperations = project.serviceOf<ExecOperations>()

    return registerInstallTask(
        name = TASK_GNU_SED_INSTALL,
        outputDirectoryProvider = layout.toolDirectory(extension.tools.map { it.gnuSed }),
        configureTask = { inputs.dir(gnuSedTmpDirectory) },
        install = { fileOperations, outputDirectory ->
            gnuSedInstall(
                fileOperations = fileOperations,
                execOperations = execOperations,
                inputDirectory = gnuSedTmpDirectory.get(),
                outputDirectory = outputDirectory
            )
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// Jextract
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading Jextract.
 */
private fun Project.registerJextractDownloadTask(
    extension: KsqliteExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_JEXTRACT_DOWNLOAD,
    extension = extension,
    url = extension.jdkVersion.zip(extension.tools) { jdkVersion, tools ->
        jextractDownloadUrl(jdkVersion = jdkVersion, jextractVersion = tools.jextract.version)
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
    extension: KsqliteExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_JEXTRACT_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = layout.toolDirectory(extension.tools.map { it.jextract }),
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
    group = ksqliteTaskGroup

    val fileOperations = serviceOf<FileSystemOperations>()
    val execOperations = serviceOf<ExecOperations>()
    val extension = ksqliteExtension
    val jextractDirectory = layout.toolDirectory(extension.tools.map { it.jextract })

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
            jextractDirectory = jextractDirectory.get().asFile,
            sqliteHeaderFile = headerFile.get().asFile,
            outputDirectory = outputDirectory.get().asFile,
            params = extension.compilationParams.get()
        )
    }
}

///////////////////////////////////////////////////////////////////////////
// SQLite
///////////////////////////////////////////////////////////////////////////

private fun String.pad2() = padStart(2, '0')

/**
 * Registers and returns the task responsible for downloading SQLite sources.
 */
private fun Project.registerSqliteDownloadTask(
    extension: KsqliteExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_SQLITE_DOWNLOAD,
    extension = extension,
    url = extension.compilationParams.zip(extension.sqliteReleaseYear) { params, releaseYear ->
        val components = params.sqliteVersion.split('.')
        val (major, minor, patch) = components
        val build = components.getOrElse(3) { "0" }
        val normalizedVersion = "$major${minor.pad2()}${patch.pad2()}${build.pad2()}"

        "https://www.sqlite.org/$releaseYear/sqlite-src-$normalizedVersion.zip"
    },
    configureVerify = { checksums ->
        algorithm("SHA3-256")
        checksum(checksums.sqlite)
    }
)

/**
 * Registers and returns the task responsible for extracting the downloaded SQLite sources.
 */
private fun Project.registerSqliteExtractTask(
    extension: KsqliteExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_SQLITE_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = downloadTaskProvider.extractDirectory(extension),
    configure = { fileOperations, outputDirectory, downloadedFile ->
        val sources = zipTree(downloadedFile)

        doLast {
            fileOperations.copyFirstDirectoryContent(sources, outputDirectory)
        }
    }
)

/**
 * Registers and returns the task responsible for downloading SQLite MC sources.
 */
private fun Project.registerSqliteMcDownloadTask(
    extension: KsqliteExtension
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
    extension: KsqliteExtension,
    downloadTaskProvider: TaskProvider<Download>
): TaskProvider<Task> = registerExtractTask(
    name = TASK_SQLITEMC_EXTRACT,
    downloadTaskProvider = downloadTaskProvider,
    outputDirectory = downloadTaskProvider.extractDirectory(extension),
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

/**
 * Registers the task responsible for installing sqlite (merging sources).
 */
private fun Project.registerSqliteInstallTask(
    extension: KsqliteExtension,
    sqliteExtractTaskProvider: TaskProvider<Task>,
    sqliteMcExtractTaskProvider: TaskProvider<Task>,
): TaskProvider<Task> {
    val sqliteDirectory = sqliteExtractTaskProvider.outputDirectory()
    val sqliteMcDirectory = sqliteMcExtractTaskProvider.outputDirectory()

    return registerInstallTask(
        name = TASK_SQLITE_INSTALL,
        outputDirectoryProvider = extension.sqliteSourcesDirectory,
        configureTask = {
            inputs.dir(sqliteDirectory)
            inputs.dir(sqliteMcDirectory)
        },
        install = { fileOperations, outputDirectory ->
            val params = extension.compilationParams.get()

            fileOperations.copy {
                from(sqliteMcDirectory)
                into(outputDirectory)
            }

            fileOperations.copy {
                from(sqliteDirectory)
                into(outputDirectory)
            }

            adjustSqliteSourceTreeForWasmCompilation(
                sqliteSourcesDirectory = outputDirectory,
                params = params
            )
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the artifacts for dynamic linking.
 */
private fun SqliteCompileTask.configureCompileTask() {
    group = ksqliteTaskGroup
    dependsOn(project.sqliteInstallTaskProvider)

    val extension = project.ksqliteExtension
    compilationParameters = extension.compilationParams
    sqliteSourcesDirectory = extension.sqliteSourcesDirectory
    checksumFile = checksumFile()
}

/**
 * Registers and returns a task responsible for generating the artifacts for dynamic linking.
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteCompileSharedTask(
    name: String,
    configure: (SqliteCompileTask.Shared.() -> Unit)? = null
): TaskProvider<SqliteCompileTask.Shared> {
    return tasks.register<SqliteCompileTask.Shared>("$TASK_SQLITE_COMPILE_SHARED$name") {
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
    configure: (SqliteCompileTask.Static.() -> Unit)? = null
): TaskProvider<SqliteCompileTask.Static> {
    return tasks.register<SqliteCompileTask.Static>("$TASK_SQLITE_COMPILE_STATIC$name") {
        configureCompileTask()
        configure?.invoke(this)
    }
}

/**
 * Registers and returns a task responsible for compiling SQLite for Wasm.
 * The returned task depends on SQLite, Emscripten, Wabt and GNU sed installations.
 */
fun Project.registerSqliteCompileWasmTask(
    outputDirectory: Provider<Directory>
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_COMPILE_WASM) {
    group = ksqliteTaskGroup

    val checksumFile = checksumFile()
    val fileOperations = serviceOf<FileSystemOperations>()
    val execOperations = serviceOf<ExecOperations>()
    val sqliteDirectory = sqliteInstallTaskProvider.outputDirectory()
    val emscriptenDirectory = emscriptenInstallTaskProvider.outputDirectory()
    val wabtDirectory = wabtInstallTaskProvider.outputDirectory()
    val gnuSedDirectory = gnuSedInstallTaskProvider.outputDirectory()
    val params = ksqliteExtension.compilationParams

    // Implicit dependency on sqlite, emscripten, wabt and GNU sed install tasks
    inputs.dir(sqliteDirectory)
    inputs.dir(emscriptenDirectory)
    inputs.dir(wabtDirectory)
    inputs.dir(gnuSedDirectory)
    outputs.dir(outputDirectory)

    doLast {
        val outputDirectory = outputDirectory.get().asFile

        executeIfChecksumChanged(checksumFile, outputDirectory::sha256) {
            fileOperations.delete { delete(outputDirectory) }
            outputDirectory.mkdirs()

            compileSqliteWasm(
                fileOperations = fileOperations,
                execOperations = execOperations,
                sqliteDirectory = sqliteDirectory.get(),
                emscriptenDirectory = emscriptenDirectory.get(),
                wabtDirectory = wabtDirectory.get(),
                gnuSedDirectory = gnuSedDirectory.get(),
                outputDirectory = outputDirectory,
                params = params.get()
            )
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Modules
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the cinterop definition file for
 * [target].
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteGenerateCInteropDefTask(
    packageName: String,
    target: SqliteTarget,
    defFile: Provider<RegularFile>
): TaskProvider<Task> = project.tasks.register(
    name = "$TASK_SQLITE_GENERATE_CINTEROP_DEF${name.uppercaseFirstChar()}"
) {
    group = ksqliteTaskGroup

    // Explicit dependency on the sqlite install task
    dependsOn(sqliteInstallTaskProvider)
    outputs.file(defFile)

    val extension = ksqliteExtension

    doFirst {
        defFile.get().asFile.parentFile.mkdirs()
    }

    doLast {
        defFile.get().asFile.writeText(
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
    group = ksqliteTaskGroup

    // Explicit dependency on sqlite install task
    dependsOn(sqliteInstallTaskProvider)

    val extension = ksqliteExtension

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
        cmakeListsFile.get().asFile.apply { parentFile.mkdirs() }.writeText(
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
 * Registers and returns the task responsible for copying SQLite JNI java sources to project.
 */
fun Project.registerSqliteCopyJniJavaSourceTask(
    sourcesDirectory: Provider<Directory>,
): TaskProvider<out Task> = project.tasks.register<Copy>(TASK_SQLITE_COPY_JNI_JAVA_SOURCES) {
    group = ksqliteTaskGroup
    dependsOn(sqliteInstallTaskProvider)

    val extension = ksqliteExtension
    val jniDirectory = fileTree(extension.sqliteSourcesDirectory.dir("ext/jni/src/org"))

    inputs.dir(jniDirectory)
    outputs.dir(sourcesDirectory)

    from(jniDirectory)
    into(sourcesDirectory.map { it.dir("org") })
}

/**
 * Registers and returns the task responsible for generating JNI runtime metadata for SQLite.
 */
fun Project.registerSqliteJniRuntimeMetadataTask(
    packageName: String,
    metadataFile: Provider<RegularFile>,
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA) {
    group = ksqliteTaskGroup
    outputs.file(metadataFile)

    val params = ksqliteExtension.compilationParams

    doLast {
        metadataFile.get().asFile.apply { parentFile.mkdirs() }.writeText(
            createSqliteJniRuntimeMetadataContent(
                packageName = packageName,
                libraryName = params.get().libraryName
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
    group = ksqliteTaskGroup
    outputs.file(metadataFile)

    val extension = ksqliteExtension

    doLast {
        metadataFile.get().asFile.apply { parentFile.mkdirs() }.writeText(
            createSqliteFfmRuntimeMetadataContent(
                packageName = packageName,
                nativeDirectoryName = nativeDirectoryName,
                libraryName = extension.compilationParams.get().libraryName,
                platforms = platforms.get()
            )
        )
    }
}