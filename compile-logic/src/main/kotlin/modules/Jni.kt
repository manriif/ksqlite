package modules

import compilation.SqliteCompilationParameters
import compilation.SqliteCompileTimeOptions
import java.io.File

/**
 * Returns the content of the SQLite CMakeLists.txt.
 */
fun createSqliteCMakeListsContent(
    cmakeVersion: String,
    sqliteHeaderFile: File,
    sqliteSourceFile: File,
    params: SqliteCompilationParameters,
): String = """
    |cmake_minimum_required(VERSION $cmakeVersion)
    |
    |add_library(${params.sqliteName} STATIC 
    |    ${sqliteSourceFile.absolutePath}
    |)
    |
    |add_library(sqlite ALIAS ${params.sqliteName})
    |
    |target_include_directories(${params.sqliteName} PUBLIC 
    |    ${sqliteHeaderFile.parentFile.absolutePath}
    |)
    |
    |target_compile_definitions(${params.sqliteName} PRIVATE
    |    SQLITE_THREADSAFE=1
    |    ${SqliteCompileTimeOptions.joinToString("\n\t")}
    |)
""".trimMargin()

/**
 * Returns the content of the JNI runtime metadata.
 */
fun createSqliteJniRuntimeMetadataContent(
    packageName: String,
    libraryName: String
): String = """
    |package $packageName
    |
    |/**
    | * Name of the Ksqlite native library.
    | */
    |public const val KSQLITE_NATIVE_LIB_NAME: String = "$libraryName"
""".trimMargin()