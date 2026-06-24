import org.gradle.api.file.CopySpec
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
 * Writes [content] to the provided file.
 */
fun Provider<RegularFile>.writeContent(content: String) {
    get().asFile.apply { parentFile.mkdirs() }.writeText(content)
}

/**
 * Replaces files described by relative [paths] from [sourceDirectory] to [destinationDirectory].
 */
fun replaceFiles(
    sourceDirectory: File,
    destinationDirectory: File,
    vararg paths: String
) {
    paths.forEach { filePath ->
        destinationDirectory.resolve(filePath).outputStream().use { output ->
            sourceDirectory.resolve(filePath).inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }
}

/**
 * Renames all files stating with [prefix] by replacing the given [prefix] with [replacer],
 * keeping the rest of the file name.
 */
fun CopySpec.replacePrefix(prefix: String, replacer: String) = rename { fileName ->
    fileName.takeIf { it.startsWith(prefix) }?.let { name ->
        replacer + name.substringAfter(prefix)
    }
}