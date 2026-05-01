package modules

import SQLITE3
import komple.project.c.CProject
import org.gradle.api.file.FileSystemOperations
import cSourceFile
import java.io.File

private const val JNI_PATH = "ext/jni/src"

/**
 * Returns the content of the SQLite CMakeLists.txt.
 */
fun createSqliteCMakeListsContent(
    cProject: CProject,
    cmakeVersion: String,
    sqliteDirectory: File,
): String {
    val libraryName = cProject.libraryName.get()
    val jniDirectory = sqliteDirectory.resolve("$JNI_PATH/c")
    val jniSourceFile = jniDirectory.resolve(cSourceFile("$SQLITE3-jni"))

    val sourceFiles = (cProject.sourceFiles + jniSourceFile)
        .joinToString("\n\t", transform = File::getAbsolutePath)

    val includeDirectories = (cProject.includeDirectories + jniDirectory)
        .joinToString("\n\t", transform = File::getAbsolutePath)

    val compileDefinitions = cProject.definitions.get().entries
        .joinToString("\n\t") { "${it.key}=${it.value}"}

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

/**
 * Copies the JNI sources from [sqliteDirectory] to [outputDirectory].
 */
fun copyJniJavaSources(
    fileOperations: FileSystemOperations,
    sqliteDirectory: File,
    outputDirectory: File
) {
    fileOperations.copy {
        from(sqliteDirectory.resolve("$JNI_PATH/org"))
        into(outputDirectory.resolve("org"))
    }
}