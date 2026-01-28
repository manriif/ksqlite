import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import sqlite.SqliteCompilerExtension

/**
 * Name of the sqlite compiler extension.
 */
const val SQLITE_COMPILER_EXTENSION_NAME = "sqliteCompiler"

/**
 * Retrieves and returns the [sqlite.SqliteCompilerExtension] from root project.
 */
val Project.sqliteCompiler: SqliteCompilerExtension
    get() = rootProject.extensions.getByName<SqliteCompilerExtension>(SQLITE_COMPILER_EXTENSION_NAME)