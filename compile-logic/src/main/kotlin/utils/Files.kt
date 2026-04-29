package utils

import org.gradle.api.file.FileSystemOperations
import java.io.File
import kotlin.io.path.createTempDirectory

///////////////////////////////////////////////////////////////////////////
// C
///////////////////////////////////////////////////////////////////////////

/**
 * Returns [fileName] with C header file extension.
 */
fun cHeaderFile(fileName: String): String {
    return "$fileName.h"
}

/**
 * Returns [fileName] with C source file extension.
 */
fun cSourceFile(fileName: String): String {
    return "$fileName.c"
}

///////////////////////////////////////////////////////////////////////////
// Copying
///////////////////////////////////////////////////////////////////////////

/**
 * Copies [directory] content into a temporary directory and returns that temporary directory.
 */
fun FileSystemOperations.copyToTempDirectory(
    directory: File,
    prefix: String? = null
): File {
    val tempDirectory = createTempDirectory(prefix ?: "ksqlite").toFile().apply {
        deleteOnExit()
    }

    copy {
        from(directory)
        into(tempDirectory)
    }

    return tempDirectory
}