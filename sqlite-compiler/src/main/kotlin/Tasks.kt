import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
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

///////////////////////////////////////////////////////////////////////////
// Root tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers the task for downloading sqlite sources.
 */
fun Project.registerTasks(extension: SqliteCompilerExtension) {
    val downloadTaskProvider = registerSqliteDownloadTask(extension)
    val checksumTaskProvider = registerSqliteChecksumTask(extension, downloadTaskProvider)
    extension.sourceTask.set(registerSqliteUnzipTask(extension, checksumTaskProvider))

    /*val generateDefFileTaskProvider = tasks.register<GenerateContentTask>("generateDefFile") {
        group = sqliteTaskGroup
        outputDirectory = sqliteDefDirectory
        dependsOn(sqliteExtractSourcesTaskProvider)

        val defContent = """
        |language = C
        |package = $localNamespace
        |headers = $sqliteName.h
        |headerFilter = $sqliteName.h
        |linkerOpts.linux_x64 = -lpthread -ldl
        |linkerOpts.macos_x64 = -lpthread -ldl
        |noStringConversion = ${defNoStringConversions.joinToString(" ") { "${sqliteName}_$it" }}
        |excludedFunctions = ${defExcludedFunctions.joinToString(" ") { "${sqliteName}_$it" }}
    """.trimMargin()

        contents = mapOf("$sqliteName.def" to defContent)
    }*/
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
// Native tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the artifacts and cinterop definition
 * file for [nativeTarget].
 */
@Suppress("NewApi")
fun SqliteCompilerExtension.registerSqliteCompilationTask(
    nativeTarget: KotlinNativeTarget,
    targetName: String,
    packageName: String
): TaskProvider<Task> = nativeTarget.project.tasks.register("$TASK_SQLITE_COMPILE$targetName") {
    group = sqliteCompilerTaskGroup

    // Use the output from source task to force implicit dependency
    val sourceDirectory = sourceTask.map { it.outputs.files.singleFile }
    val outputDirectory = sqliteNativeLibDirectory.map { it.dir(nativeTarget.konanTarget.name) }

    val inputComponents = sourceDirectory.zip(sqliteRelease) { directory, release ->
        directory to release.sqliteMcName
    }

    val outputComponents = outputDirectory.zip(sqliteRelease) { directory, release ->
        directory.asFile to release.sqliteName
    }

    val headerFile = inputComponents.map { it.first.resolve("${it.second}.h") }
    val sourceFile = inputComponents.map { it.first.resolve("${it.second}.c") }

    // Implicit dependency on the source task
    inputs.files(headerFile, sourceFile)
    outputs.dir(outputDirectory)

    val defFile = outputComponents.map { it.first.resolve("${it.second}.def") }
    val objectFile = outputComponents.map { it.first.resolve("${it.second}.o") }
    val libraryFile = outputComponents.map { it.first.resolve("lib${it.second}.a") }
    val compilerFlags = getNativeCompilerFlags(nativeTarget.konanTarget)
    val compiler = getNativeCompiler(nativeTarget.konanTarget)
    val archiver = getNativeArchiver(nativeTarget.konanTarget)

    val fileOperations = project.serviceOf<FileSystemOperations>()
    val execOperations = project.serviceOf<ExecOperations>()
    val layout = project.serviceOf<ProjectLayout>()

    doFirst {
        fileOperations.delete {
            delete(outputDirectory)
        }

        outputDirectory.get().asFile.mkdirs()
    }

    doLast {
        defFile.get().writeText(
            createDefContent(
                packageName = packageName,
                libraryPath = outputDirectory.get().asFile.absolutePath,
                release = sqliteRelease.get()
            )
        )

        execOperations.exec {
            workingDir = layout.projectDirectory.asFile

            commandLine(
                compiler,
                *compilerFlags.toTypedArray(),
                "-c",
                sourceFile.get().absolutePath,
                "-o",
                objectFile.get().absolutePath,
                *SqliteCompileTimeOptions,
                "-O3"
            )
        }

        execOperations.exec {
            workingDir = layout.projectDirectory.asFile

            commandLine(
                archiver,
                "rcs",
                libraryFile.get().absolutePath,
                objectFile.get().absolutePath
            )
        }
    }
}