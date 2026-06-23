package tools.sqlite

import SQLITE3
import SQLITE3MC_AMALGAMATION
import SQLITE_VERSION_FILE
import komple.task.hasChanged
import komple.task.install.InstallTask
import modules.configureSqliteWasmTrunk
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Task responsible for installing SQLite.
 */
@CacheableTask
internal abstract class SqliteInstallTask : InstallTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ksqliteDirectory: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sqliteMcDirectory: DirectoryProperty

    @TaskAction
    fun install() {
        val tracker = tracker.get()

        if (!tracker.hasChanged()) {
            didWork = false
            return logger.lifecycle("Reusing previously installed SQLite")
        }

        val inputDirectory = inputDirectory.get().asFile
        val outputDirectory = outputDirectory.get().asFile

        fileOperations.delete {
            delete(outputDirectory)
        }

        fileOperations.copy {
            from(sqliteMcDirectory) {
                include { it.name.startsWith(SQLITE3MC_AMALGAMATION) }

                // Starting from version 3.53.2, SQLite Multiple Ciphers stopped publishing the
                // sqlite3.c and sqlite3.h in a simplification effort
                // However that broken WASM build so we now instead rename the amalgamation files to
                // sqlite3.c and sqlite3.h and this has the advantage of simplifying our own logic
                // too
                rename { fileName ->
                    fileName.takeIf { it.startsWith(SQLITE3MC_AMALGAMATION) }?.let { name ->
                        SQLITE3 + name.substringAfter(SQLITE3MC_AMALGAMATION)
                    }
                }
            }

            from(inputDirectory)
            into(outputDirectory)

            // This file is problematic with C++ as some headers include a <version> header
            // which is resolved as this file due to case insensitivity on some file systems
            exclude(SQLITE_VERSION_FILE)
        }

        configureSqliteWasmTrunk(
            ksqliteDirectory = ksqliteDirectory.get().asFile,
            sqliteDirectory = outputDirectory
        )
    }
}