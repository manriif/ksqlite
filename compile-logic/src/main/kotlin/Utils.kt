import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
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
// Files
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

/**
 * Deletes [directory] and its content and returns the [File] to the [directory].
 */
fun FileSystemOperations.clearAndGetFile(directory: Provider<Directory>): File {
    return directory.get().asFile.also { directory ->
        delete { delete(directory) }
        directory.mkdirs()
    }
}

/**
 * Writes [content] to the provided file.
 */
fun Provider<RegularFile>.writeContent(content: String) {
    get().asFile.apply { parentFile.mkdirs() }.writeText(content)
}