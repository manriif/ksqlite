package modules

import komple.project.c.CProject
import java.io.File

/**
 * Returns the content of the SQLite CMakeLists.txt.
 */
fun createSqliteCMakeListsContent(
    cProject: CProject,
    cmakeVersion: String,
): String {
    val libraryName = cProject.libraryName.get()

    val sourceFiles = cProject.sourceFiles
        .joinToString("\n\t", transform = File::getAbsolutePath)

    val includeDirectories = cProject.includeDirectories
        .joinToString("\n\t", transform = File::getAbsolutePath)

    val compileDefinitions = cProject.definitions().get()
        .joinToString("\n\t")

    return """
        |cmake_minimum_required(VERSION $cmakeVersion)
        |
        |add_library($libraryName STATIC 
        |    $sourceFiles
        |)
        |
        |target_include_directories($libraryName PUBLIC 
        |    $includeDirectories
        |)
        |
        |target_compile_definitions($libraryName PRIVATE
        |    SQLITE_THREADSAFE=1
        |    $compileDefinitions
        |)
    """.trimMargin()
}

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