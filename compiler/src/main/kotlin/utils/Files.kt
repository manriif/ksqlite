package utils

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.WorkResult

/**
 * Resolves the directory for [path] relatively to this property directory and returns the absolute
 * path to the resolved directory.
 */
fun DirectoryProperty.absolutePath(path: String): String {
    return map { it.dir(path) }.get().asFile.absolutePath
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