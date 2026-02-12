package utils

import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.WorkResult
import java.io.File
import java.security.MessageDigest
import kotlin.io.path.createTempDirectory

/**
 * Resolves the directory for [path] relatively to this property directory and returns the absolute
 * path to the resolved directory.
 */
fun Provider<Directory>.absolutePath(path: String): String {
    return map { it.dir(path) }.get().asFile.absolutePath
}

///////////////////////////////////////////////////////////////////////////
// Hashing
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the SHA-256 hash of this file or directory.
 */
fun File.sha256(): String {
    val messageDigest = when {
        isFile -> MessageDigest.getInstance("SHA-256").apply {
            update(readBytes())
        }

        isDirectory -> MessageDigest.getInstance("SHA-256").apply {
            walkTopDown()
                .filter { it.isFile }
                .sortedBy { it.relativeTo(this@sha256).path }
                .forEach { file ->
                    update(file.relativeTo(this@sha256).path.toByteArray())
                    update(file.readBytes())
                }
        }

        else -> error("File is not a regular file nor a directory")
    }

    return messageDigest.digest().joinToString("") { "%02x".format(it) }
}

/**
 * Returns the SHA-256 hash of this files.
 */
fun List<File>.sha256(): String {
    val messageDigest = MessageDigest.getInstance("SHA-256").apply {
        sortedBy { it.path }.forEach { file ->
            update(file.path.toByteArray())
            update(file.readBytes())
        }
    }

    return messageDigest.digest().joinToString("") { "%02x".format(it) }
}

///////////////////////////////////////////////////////////////////////////
// Copying
///////////////////////////////////////////////////////////////////////////

/**
 * Copy the content of the first directory represented by [source] to [destination].
 */
fun FileSystemOperations.copyFirstDirectoryContent(
    source: FileTree,
    destination: Provider<Directory>
): WorkResult = copy {
    from(source) {
        eachFile {
            val segments = relativePath.segments

            if (segments.size > 1) {
                relativePath = RelativePath(true, *segments.drop(1).toTypedArray())
            } else {
                exclude()
            }
        }
    }

    into(destination)
    includeEmptyDirs = false
}

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