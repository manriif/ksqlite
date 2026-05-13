package modules

import SQLITE3
import SQLITE3_MC_AMALGAMATION
import cSourceFile
import komple.project.c.CProject
import org.gradle.api.file.FileSystemOperations
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Sources
///////////////////////////////////////////////////////////////////////////

private const val SQLITE3_JNI_PACKAGE = "org/sqlite/jni"
private const val SQLITE3_JNI_PACKAGE_ANNOTATION = "$SQLITE3_JNI_PACKAGE/annotation"
private const val SQLITE3_JNI_PACKAGE_CAPI = "$SQLITE3_JNI_PACKAGE/capi"
private const val SQLITE3_JNI_PACKAGE_FTS5 = "$SQLITE3_JNI_PACKAGE/fts5"

private const val JNI_SRC_PATH = "ext/jni/src"
private const val JNI_SRC_PATH_C = "$JNI_SRC_PATH/c"

const val SQLITE3_JNI = "$SQLITE3-jni"

/**
 * Performs some adjustments and fixes for JNI.
 */
fun configureSqliteJniTrunk(sqliteDirectory: File) {
    patchCapiFile(sqliteDirectory)
}

private fun patchCapiFile(sqliteDirectory: File) {
    val capiFile = sqliteDirectory.resolve("$JNI_SRC_PATH/$SQLITE3_JNI_PACKAGE_CAPI/CApi.java")
    val content = capiFile.readText()
    val toFind = """System.loadLibrary("$SQLITE3_JNI");"""
    val index = content.indexOf(toFind)

    check(index != -1) {
        "Load library not found on CApi file"
    }

    capiFile.outputStream().writer().use { stream ->
        stream.write(content.substring(0, index))
        stream.write("""//$toFind""")
        stream.write(content.substring(index + toFind.length))
    }
}

///////////////////////////////////////////////////////////////////////////
// Tasks
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the content of the SQLite CMakeLists.txt.
 */
fun createSqliteCMakeListsContent(
    cProject: CProject,
    cmakeVersion: String,
    sqliteDirectory: File
): String {
    val libraryName = SQLITE3_JNI
    val jniSourceDirectory = sqliteDirectory.resolve(JNI_SRC_PATH_C)
    val jniSourceFile = jniSourceDirectory.resolve(cSourceFile(SQLITE3_JNI))

    val sourceFiles = listOf(jniSourceFile)
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
        |    SQLITE_C=${cSourceFile(SQLITE3_MC_AMALGAMATION)}
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