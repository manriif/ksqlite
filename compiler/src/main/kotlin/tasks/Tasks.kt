package tasks

import KsqliteChecksums
import KsqliteCompilerExtension
import androidToolchain
import compilation.SqliteTarget
import compilation.compileSqliteWasm
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.VerifyAction
import emscriptenInstallTaskProvider
import jextractInstallTaskProvider
import ksqliteCompilerExtension
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
import toolchainDirectory
import toolchains.androidNdk
import toolchains.androidNdkDownloadUrl
import toolchains.androidNdkExtract
import tools.emscriptenInstall
import tools.emsdkDownloadUrl
import tools.emsdkExtract
import tools.jextract
import tools.jextractDownloadUrl
import tools.jextractExtract
import tools.jextractGenerateBindings
import utils.copyFirstDirectoryContent

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val ksqliteCompilerTaskGroup = "ksqlite"

const val TASK_TOOLCHAIN_ANDROID_DOWNLOAD = "toolchainAndroidDownload"
const val TASK_TOOLCHAIN_ANDROID_EXTRACT = "toolchainAndroidExtract"
const val TASK_EMSCRIPTEN_INSTALL = "emscriptenInstall"
const val TASK_EMSCRIPTEN_DOWNLOAD = "emscriptenDownload"
const val TASK_EMSCRIPTEN_EXTRACT = "emscriptenExtract"
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

    registerEmscriptenInstallTask(
        extension = extension,
        emsdkExtractTaskProvider = registerEmscriptenExtractTask(
            extension = extension,
            downloadTaskProvider = registerEmscriptenDownloadTask(extension)
        )
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
    fileNamePrefix: String = "",
    configureVerify: VerifyAction.(KsqliteChecksums) -> Unit,
): TaskProvider<Download> = tasks.register<Download>(name) {
    group = ksqliteCompilerTaskGroup

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

/**
 * Returns a directory where to extract downloaded file in the download directory.
 */
private fun TaskProvider<Download>.extractDirectory(
    extension: KsqliteCompilerExtension
): Provider<Directory> = zip(extension.downloadDirectory) { task, directory ->
    directory.dir(task.dest.nameWithoutExtension)
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
// Emscripten
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for downloading Emsdk.
 */
private fun Project.registerEmscriptenDownloadTask(
    extension: KsqliteCompilerExtension
): TaskProvider<Download> = registerDownloadTask(
    name = TASK_EMSCRIPTEN_DOWNLOAD,
    extension = extension,
    url = extension.emscripten.map { emsdk ->
        emsdkDownloadUrl(emscriptenVersion = emsdk.version)
    },
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
    extension: KsqliteCompilerExtension,
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
    extension: KsqliteCompilerExtension,
    emsdkExtractTaskProvider: TaskProvider<Task>,
): TaskProvider<Task> = project.tasks.register(TASK_EMSCRIPTEN_INSTALL) {
    group = ksqliteCompilerTaskGroup

    val outputDirectory = extension.emscripten.flatMap { it.path }
    val emsdkDirectory = emsdkExtractTaskProvider.map { it.outputs.files.singleFile }

    inputs.dir(emsdkDirectory)
    outputs.dir(outputDirectory)

    emscriptenInstall(
        inputDirectory = layout.dir(emsdkDirectory),
        outputDirectory = outputDirectory
    )
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
    url = extension.jdkVersion.zip(extension.jextract) { jdkVersion, jextract ->
        jextractDownloadUrl(jdkVersion = jdkVersion, jextractVersion = jextract.version)
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
    outputDirectory = extension.jextract.flatMap { it.path },
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
            jextractDirectory = extension.jextract.get().path.get().asFile,
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
    outputDirectory = downloadTaskProvider.extractDirectory(extension),
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

    val outputDirectory = extension.sqliteSourcesDirectory
    val sqliteDirectory = sqliteExtractTaskProvider.map { it.outputs.files.singleFile }
    val sqliteMcDirectory = sqliteMcExtractTaskProvider.map { it.outputs.files.singleFile }
    val fileOperations = serviceOf<FileSystemOperations>()

    inputs.dir(sqliteDirectory)
    inputs.dir(sqliteMcDirectory)
    outputs.dir(outputDirectory)

    doFirst {
        fileOperations.delete {
            delete(outputDirectory)
        }
    }

    doLast {
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
            sqliteSourcesDirectory = outputDirectory.get().asFile,
            params = params
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

/**
 * Registers and returns a task responsible for compiling SQLite for Wasm.
 * The returned task depends on Emscripten and SQLite installation.
 */
fun Project.registerSqliteCompileWasmTask(
    outputDirectory: Provider<Directory>
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_COMPILE_WASM) {
    group = ksqliteCompilerTaskGroup

    val fileOperations = serviceOf<FileSystemOperations>()
    val execOperations = serviceOf<ExecOperations>()
    val sqliteDirectory = sqliteInstallTaskProvider.map { it.outputs.files.singleFile }
    val emscriptenDirectory = emscriptenInstallTaskProvider.map { it.outputs.files.singleFile }
    val extension = ksqliteCompilerExtension

    // Implicit dependency on sqlite and emscripten install tasks
    inputs.dir(sqliteDirectory)
    inputs.dir(emscriptenDirectory)
    outputs.dir(outputDirectory)

    doLast {
        compileSqliteWasm(
            fileOperations = fileOperations,
            execOperations = execOperations,
            sqliteDirectory = sqliteDirectory.get(),
            emscriptenDirectory = emscriptenDirectory.get(),
            outputDirectory = outputDirectory.get().asFile,
            params = extension.compilationParams.get()
        )
    }

    doFirst {
        fileOperations.delete {
            delete(outputDirectory)
        }

        outputDirectory.get().asFile.mkdirs()
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
    defFile: Provider<RegularFile>
): TaskProvider<Task> = project.tasks.register(
    name = "$TASK_SQLITE_GENERATE_CINTEROP_DEF${name.uppercaseFirstChar()}"
) {
    group = ksqliteCompilerTaskGroup

    // Explicit dependency on the sqlite install task
    dependsOn(sqliteInstallTaskProvider)
    outputs.file(defFile)

    val extension = ksqliteCompilerExtension

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

    doFirst {
        cmakeListsFile.get().asFile.parentFile.mkdirs()
    }

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
 * Registers and returns the task responsible for copying SQLite JNI java sources to project.
 */
fun Project.registerSqliteCopyJniJavaSourceTask(
    sourcesDirectory: Provider<Directory>,
): TaskProvider<out Task> = project.tasks.register<Copy>(TASK_SQLITE_COPY_JNI_JAVA_SOURCES) {
    group = ksqliteCompilerTaskGroup
    dependsOn(sqliteInstallTaskProvider)

    val extension = ksqliteCompilerExtension
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
    group = ksqliteCompilerTaskGroup
    outputs.file(metadataFile)

    val extension = ksqliteCompilerExtension

    doFirst {
        metadataFile.get().asFile.parentFile.mkdirs()
    }

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

    doFirst {
        metadataFile.get().asFile.parentFile.mkdirs()
    }

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