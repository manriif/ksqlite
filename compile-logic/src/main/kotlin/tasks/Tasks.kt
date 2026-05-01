package tasks
/*
import ksqliteExtension
import modules.createSqliteCMakeListsContent
import modules.createSqliteJniRuntimeMetadataContent
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

const val ksqliteTaskGroup = "ksqlite"

const val TASK_SQLITE_GENERATE_CMAKE_LISTS = "sqliteGenerateCMakeLists"
const val TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA = "sqliteGenerateJniRuntimeMetadata"
const val TASK_SQLITE_COPY_JNI_JAVA_SOURCES = "sqliteCopyJniJavaSources"

///////////////////////////////////////////////////////////////////////////
// Modules
///////////////////////////////////////////////////////////////////////////

/**
 * Registers and returns the task responsible for generating the CMakeList.txt file for SQLite.
 * The returned task depends on SQLite installation.
 */
fun Project.registerSqliteJniGenerateCMakeListsTask(
    cmakeListsFile: Provider<RegularFile>,
    cmakeVersion: String
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_CMAKE_LISTS) {
    group = ksqliteTaskGroup

    // Explicit dependency on sqlite install task
    dependsOn(sqliteInstallTaskProvider)

    val extension = ksqliteExtension
    val jniDirectory = extension.sqliteDirectory.dir("ext/jni/src/c")
    val jniHeaderFile = jniDirectory.map { it.file("sqlite3-jni.h") }
    val jniSourceFile = jniDirectory.map { it.file("sqlite3-jni.c") }
    val headerFile = extension.ksqliteHeaderFile()
    val sourceFiles = ksqliteSourceFiles(extension).from(jniSourceFile)

    inputs.files(jniHeaderFile, headerFile, sourceFiles)
    outputs.file(cmakeListsFile)

    doLast {
        cmakeListsFile.get().asFile.apply { parentFile.mkdirs() }.writeText(
            createSqliteCMakeListsContent(
                cmakeVersion = cmakeVersion,
                includeDirectories = listOf(jniHeaderFile, headerFile)
                    .map { it.get().asFile.parentFile },
                sourceFiles = sourceFiles.files,
                params = extension.sqliteComponents.get()
            )
        )
    }
}

/**
 * Registers and returns the task responsible for copying SQLite JNI java sources to project.
 */
fun Project.registerSqliteCopyJniJavaSourceTask(
    sourcesDirectory: Provider<Directory>,
): TaskProvider<out Task> = project.tasks.register<Copy>(TASK_SQLITE_COPY_JNI_JAVA_SOURCES) {
    group = ksqliteTaskGroup
    dependsOn(sqliteInstallTaskProvider)

    val extension = ksqliteExtension
    val jniDirectory = fileTree(extension.sqliteDirectory.dir("ext/jni/src/org"))

    inputs.dir(jniDirectory)
    outputs.dir(sourcesDirectory)

    from(jniDirectory)
    into(sourcesDirectory.map { it.dir("org") })
}

/**
 * Registers and returns the task responsible for generating JNI runtime metadata for SQLite.
 */
fun Project.registerSqliteJniRuntimeMetadataTask(
    packageName: String,
    metadataFile: Provider<RegularFile>,
): TaskProvider<Task> = project.tasks.register(TASK_SQLITE_GENERATE_JNI_RUNTIME_METADATA) {
    group = ksqliteTaskGroup
    outputs.file(metadataFile)

    val params = ksqliteExtension.sqliteComponents

    doLast {
        metadataFile.get().asFile.apply { parentFile.mkdirs() }.writeText(
            createSqliteJniRuntimeMetadataContent(
                packageName = packageName,
                libraryName = params.get().libraryName
            )
        )
    }
}*/