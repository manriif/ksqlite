package tasks
/*
import KsqliteExtension
import emscriptenInstallTaskProvider
import gnuSedInstallTaskProvider
import ksqliteExtension
import ksqliteHeaderFile
import ksqliteSourceFiles
import modules.createDefContent
import modules.createSqliteCMakeListsContent
import modules.createSqliteFfmRuntimeMetadataContent
import modules.createSqliteJniRuntimeMetadataContent
import modules.patchGeneratedSqliteForWasm
import modules.sqliteWasmExtraResourceFileNames
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
import komple.platform.Platform
import sqliteInstallTaskProvider
import wabtInstallTaskProvider
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val ksqliteTaskGroup = "ksqlite"

const val TASK_TOOLCHAIN_ANDROID_INSTALL = "toolchainAndroidInstall"
const val TASK_EMSCRIPTEN_INSTALL = "emscriptenInstall"
const val TASK_WABT_INSTALL = "wabtInstall"
const val TASK_GNU_SED_INSTALL = "gnuSedInstall"
const val TASK_SQLITE_INSTALL = "sqliteInstall"
const val TASK_SQLITE_COMPILE_SHARED = "sqliteCompileShared"
const val TASK_SQLITE_COMPILE_STATIC = "sqliteCompileStatic"
const val TASK_SQLITE_COMPILE_WASM = "sqliteCompileWasm"
const val TASK_SQLITE_GENERATE_CINTEROP_DEF = "sqliteGenerateCInteropDef"
const val TASK_SQLITE_GENERATE_CMAKE_LISTS = "sqliteGenerateCMakeLists"
const val TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA = "sqliteGenerateJniRuntimeMetadata"
const val TASK_SQLITE_GENERATE_FFM_RUNTIME_METADATA = "sqliteGenerateFfmRuntimeMetadata"
const val TASK_SQLITE_COPY_JNI_JAVA_SOURCES = "sqliteCopyJniJavaSources"
const val TASK_SQLITE_COPY_WASM_RESOURCES = "sqliteCopyWasmResources"

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
private fun Provider<Download>.extractDirectory(
    extension: KsqliteExtension
): Provider<Directory> = zip(extension.downloadDirectory) { task, directory ->
    directory.dir(task.dest.nameWithoutExtension)
}

/**
 * Returns a provider resolving the output directory of the task.
 */
private fun Provider<Task>.outputDirectory(): Provider<File> {
    return map { it.outputs.files.singleFile }
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
    compilationParameters = extension.sqliteComponents
    checksumFile = checksumFile()
    sourceFiles = project.ksqliteSourceFiles(extension)
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
    val params = ksqliteExtension.sqliteComponents

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
    targetName: String,
    target: SqliteTarget,
    defFile: Provider<RegularFile>
): TaskProvider<Task> = project.tasks.register(
    name = "$TASK_SQLITE_GENERATE_CINTEROP_DEF${targetName.uppercaseFirstChar()}"
) {
    group = ksqliteTaskGroup

    // Explicit dependency on the sqlite install task
    dependsOn(sqliteInstallTaskProvider)

    val extension = ksqliteExtension
    val headerFile = extension.ksqliteHeaderFile()

    val sqliteMcHeaderFile = extension.run {
        sqliteDirectory.zip(sqliteComponents) { directory, params ->
            directory.file("${params.sqliteMcAmalgamationName}.h")
        }
    }

    outputs.file(defFile)
    inputs.file(headerFile)

    doLast {
        defFile.get().asFile.parentFile.mkdirs()

        defFile.get().asFile.writeText(
            createDefContent(
                packageName = packageName,
                libraryFile = target.libraryFile.get().asFile,
                headerFile = headerFile.get().asFile,
                sqliteMcHeaderFile = sqliteMcHeaderFile.get().asFile,
                operatingSystem = target.platform.get().operatingSystem,
                params = extension.sqliteComponents.get()
            )
        )
    }
}

/**
 * Registers and returns the task responsible for generating the CMakeList.txt file for SQLite.
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteJniGenerateCMakeListsTask(
    cmakeListsFile: Provider<RegularFile>,
    cmakeVersion: String
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_CMAKE_LISTS) {
    group = ksqliteTaskGroup

    // Explicit dependency on sqlite install task
    dependsOn(sqliteInstallTaskProvider)

    val extension = ksqliteExtension
    val jniDirectory = extension.sqliteDirectory.dir("ext/jni/src/c")
    val jniHeaderFile = jniDirectory.map { it.file("sqlite3-jni.h") }
    val jniSourceFile = jniDirectory.map { it.file("sqlite3-jni.c") }
    val headerFile = extension.ksqliteHeaderFile()
    val sourceFiles = ksqliteSourceFiles(extension).from(jniSourceFile)

    inputs.files(jniHeaderFile, headerFile, sourceFiles)
    outputs.file(cmakeListsFile)

    doLast {
        cmakeListsFile.get().asFile.apply { parentFile.mkdirs() }.writeText(
            createSqliteCMakeListsContent(
                cmakeVersion = cmakeVersion,
                includeDirectories = listOf(jniHeaderFile, headerFile)
                    .map { it.get().asFile.parentFile },
                sourceFiles = sourceFiles.files,
                params = extension.sqliteComponents.get()
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
    val jniDirectory = fileTree(extension.sqliteDirectory.dir("ext/jni/src/org"))

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

    val params = ksqliteExtension.sqliteComponents

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
 * Registers and returns the task responsible for copying the necessary resources for WASM SQLite.
 */
fun Project.registerSqliteCopyWasmResourcesTask(
    wasmCompileTaskProvider: Provider<Task>,
    outputDirectory: Provider<Directory>
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_COPY_WASM_RESOURCES) {
    group = ksqliteTaskGroup

    // Implicit dependency on wasmCompileTaskProvider
    val inputDirectory = wasmCompileTaskProvider.outputDirectory()
    val esm64Directory = inputDirectory.map { it.resolve("esm64") }
    val params = ksqliteExtension.sqliteComponents

    val sqliteSourceFile = esm64Directory.zip(params) { directory, params ->
        directory.resolve("${params.sqliteName}-64bit.mjs")
    }

    val fileOperations = project.serviceOf<FileSystemOperations>()

    outputs.dir(outputDirectory)

    doLast {
        fileOperations.delete {
            delete(outputDirectory)
        }

        val outputDirectoryFile = outputDirectory.get().asFile.apply { mkdirs() }
        val sqliteFile = sqliteSourceFile.get()
        val sqliteName = params.get().sqliteName

        patchGeneratedSqliteForWasm(
            sqliteName = sqliteName,
            inputFile = sqliteFile,
            outputFile = outputDirectoryFile.resolve(sqliteFile.name)
        )

        fileOperations.copy {
            from(esm64Directory) {
                include { it.name != sqliteFile.name }
            }

            sqliteWasmExtraResourceFileNames(sqliteName).takeUnless { it.isEmpty() }?.let { names ->
                from(inputDirectory) {
                    include { !it.isDirectory && it.name in names }
                }
            }

            into(outputDirectoryFile)
        }
    }
}*/