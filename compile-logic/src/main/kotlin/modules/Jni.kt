package modules

import compilation.SqliteCompilationParameters
import compilation.SqliteDefines
import java.io.File

/**
 * Returns the content of the SQLite CMakeLists.txt.
 */
fun createSqliteCMakeListsContent(
    cmakeVersion: String,
    includeDirectories: Collection<File>,
    sourceFiles: Collection<File>,
    params: SqliteCompilationParameters,
): String = """
    |cmake_minimum_required(VERSION $cmakeVersion)
    |
    |add_library(${params.libraryName} STATIC 
    |    ${sourceFiles.joinToString("\n\t", transform = File::getAbsolutePath)}
    |)
    |
    |target_include_directories(${params.libraryName} PUBLIC 
    |    ${includeDirectories.joinToString("\n\t", transform = File::getAbsolutePath)}
    |)
    |
    |target_compile_definitions(${params.libraryName} PRIVATE
    |    SQLITE_THREADSAFE=1
    |    ${SqliteDefines.joinToString("\n\t")}
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