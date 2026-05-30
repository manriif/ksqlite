package modules

import komple.project.c.CProject
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Build
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the arguments for CMake.
 */
fun CProject.cmakeArguments(): List<String> {
    val sourceFilesPaths = sourceFiles
        .joinToString(";", transform = File::getAbsolutePath)

    val includeDirectoriesPaths = includeDirectories
        .joinToString(";", transform = File::getAbsolutePath)

    val compileDefinitions = definitions.get().entries
        .joinToString(";") { "${it.key}=${it.value}" }

    return listOf(
        "-DKSQLITE_LIB_NAME=${libraryName.get()}",
        "-DKSQLITE_INCLUDES=$includeDirectoriesPaths",
        "-DKSQLITE_SOURCES=$sourceFilesPaths",
        "-DKSQLITE_DEFINITIONS=$compileDefinitions"
    )
}

///////////////////////////////////////////////////////////////////////////
// Sources
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the content of the JNI runtime metadata.
 */
fun createSqliteJniRuntimeMetadataContent(cProject: CProject): String {
    return """
        |package ${cProject.packageName.get()}
        |
        |/**
        | * Name of the Ksqlite native library.
        | */
        |public const val KSQLITE_NATIVE_LIB_NAME: String = "${cProject.libraryName.get()}"
    """.trimMargin()
}