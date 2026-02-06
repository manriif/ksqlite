package utils

import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.WorkResult
import java.io.File
import java.security.MessageDigest

/**
 * Resolves the directory for [path] relatively to this property directory and returns the absolute
 * path to the resolved directory.
 */
fun Provider<Directory>.absolutePath(path: String): String {
    return map { it.dir(path) }.get().asFile.absolutePath
}

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
 * Inserts content after multiple different search texts in sequence and returns `true` if all
 * search texts were found and content was inserted, or `false` if any search text was not found.
 * The file is only modified if ALL search texts are found.
 *
 * @param searchTexts List of texts to search for sequentially
 * @param contentToInsert Content to insert after each found search text
 * @return `true` if all insertions succeeded, `false` if any search text was not found
 */
fun File.insertAfterText(
    searchTexts: List<String>,
    contentToInsert: String
): Boolean {
    val originalContent = readText()
    var modifiedContent = originalContent

    for (searchText in searchTexts) {
        val index = modifiedContent.indexOf(searchText)

        if (index == -1) {
            return false // Search text not found, abort without modifying file
        }

        val insertPosition = index + searchText.length

        modifiedContent = modifiedContent.substring(0, insertPosition) +
                contentToInsert +
                modifiedContent.substring(insertPosition)
    }

    writeText(modifiedContent)
    return true
}