package sqlite

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val sqliteCompilerTaskGroup = "compile sqlite"

const val SQLITE_TASK_DOWNLOAD_SOURCE_TREE = "sqliteDownloadSourceTree"
const val SQLITE_TASK_CHECKSUM_SOURCE_TREE = "sqliteChecksumSourceTree"

///////////////////////////////////////////////////////////////////////////
// Tasks
///////////////////////////////////////////////////////////////////////////


/**
 * Registers the task for downloading sqlite sources.
 */
fun Project.registerTasks(extension: SqliteCompilerExtension) {
    val downloadTaskProvider = registerDownloadSqliteSourceTreeTask(extension)
    val checksumTaskProvider = registerChecksumSqliteSourceTreeTask(downloadTaskProvider, extension)

    /*val unzipTaskProvider = tasks.register<Copy>("sqliteUnzip") {
        group = ksqliteTaskGroup
        dependsOn(sqliteChecksumTaskProvider)
        from(zipTree(sqliteDownloadTaskProvider.map { it.dest }))
        into(sqliteDownloadDirectory)
    }

    val extractSourcesTaskProvider = tasks.register<Copy>("sqliteExtractSources") {
        group = ksqliteTaskGroup
        dependsOn(sqliteUnzipTaskProvider)
        from(sqliteDownloadDirectory.map { it.dir(sqliteReleaseFileName) })
        into(sqliteSourcesDirectory)
        include("$sqliteName.h", "$sqliteName.c")
    }


    val generateDefFileTaskProvider = tasks.register<GenerateContentTask>("generateDefFile") {
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
 * Registers and returns the task responsible for downloading SQLite canonical source tree.
 */
private fun Project.registerDownloadSqliteSourceTreeTask(
    extension: SqliteCompilerExtension
): TaskProvider<Download> {
    return tasks.register<Download>(SQLITE_TASK_DOWNLOAD_SOURCE_TREE) {
        group = sqliteCompilerTaskGroup

        val sourceTreeFileName = extension.sqliteRelease.map { release ->
            "sqlite-src-${release.normalizedVersion}"
        }

        src(extension.sqliteRelease.zip(sourceTreeFileName) { release, fileName ->
            "https://sqlite.org/${release.year}/${fileName}.zip"
        })

        dest(extension.sqliteDownloadDirectory.zip(sourceTreeFileName) { directory, fileName ->
            directory.file(fileName)
        })

        overwrite(false)
    }
}

/**
 * Registers and returns the task responsible for checking downloaded SQLite source tree integrity.
 */
private fun Project.registerChecksumSqliteSourceTreeTask(
    downloadTaskProvider: TaskProvider<Download>,
    extension: SqliteCompilerExtension
): TaskProvider<Verify> {
    return tasks.register<Verify>(SQLITE_TASK_CHECKSUM_SOURCE_TREE) {
        group = sqliteCompilerTaskGroup

        // Implicit dependency
        src(downloadTaskProvider.map { it.dest })
        algorithm("SHA3-256")

        // Unfortunately, verify task do not accept provider
        checksum(extension.sqliteRelease.get().checksum)
    }
}