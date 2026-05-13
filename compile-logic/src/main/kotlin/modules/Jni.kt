package modules

import SQLITE3
import cSourceFile
import komple.project.c.CProject
import org.gradle.api.file.FileSystemOperations
import java.io.File

private const val SQLITE3_JNI_PACKAGE = "org/sqlite/jni"
private const val SQLITE3_JNI_PACKAGE_ANNOTATION = "$SQLITE3_JNI_PACKAGE/annotation"
private const val SQLITE3_JNI_PACKAGE_CAPI = "$SQLITE3_JNI_PACKAGE/capi"
private const val SQLITE3_JNI_PACKAGE_FTS5 = "$SQLITE3_JNI_PACKAGE/fts5"

private const val JNI_SRC_PATH = "ext/jni/src"
private const val JNI_SRC_PATH_C = "$JNI_SRC_PATH/c"

private const val SQLITE3_JNI = "$SQLITE3-jni"

/**
 * Returns the content of the SQLite CMakeLists.txt.
 */
fun createSqliteCMakeListsContent(
    cProject: CProject,
    cmakeVersion: String,
    sqliteDirectory: File
): String {
    val libraryName = cProject.libraryName.get()
    val jniSourceDirectory = sqliteDirectory.resolve(JNI_SRC_PATH_C)
    val jniSourceFile = jniSourceDirectory.resolve(cSourceFile(SQLITE3_JNI))

    val sourceFiles = (cProject.sourceFiles + jniSourceFile)
        .joinToString("\n\t", transform = File::getAbsolutePath)

    val includeDirectories = (cProject.includeDirectories + jniSourceDirectory)
        .joinToString("\n\t", transform = File::getAbsolutePath)

    val compileDefinitions = cProject.definitions.get().entries
        .joinToString("\n\t") { "${it.key}=${it.value}" }

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
        |    JNI_VERSION_1_8=0x00010008
        |    SQLITE_THREADSAFE=1
        |    SQLITE_JNI_FATAL_OOM=1
        |    SQLITE_JNI_ENABLE_METRICS=1
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
    val jniSrcDirectory = sqliteDirectory.resolve(JNI_SRC_PATH)

    fileOperations.copy {
        from(jniSrcDirectory.resolve(SQLITE3_JNI_PACKAGE_ANNOTATION)) {
            into(SQLITE3_JNI_PACKAGE_ANNOTATION)
        }

        from(jniSrcDirectory.resolve(SQLITE3_JNI_PACKAGE_CAPI)) {
            exclude { element ->
                element.name == "Tester1.java" || element.name == "SQLTester.java"
            }

            into(SQLITE3_JNI_PACKAGE_CAPI)
        }

        from(jniSrcDirectory.resolve(SQLITE3_JNI_PACKAGE_FTS5)) {
            exclude { element ->
                element.name == "TesterFts5.java"
            }

            into(SQLITE3_JNI_PACKAGE_FTS5)
        }

        exclude { element ->
            element.name == "package-info.java"
        }

        into(outputDirectory)
    }
}