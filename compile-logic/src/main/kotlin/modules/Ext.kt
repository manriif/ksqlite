package modules

import java.io.File

const val GENERATED_ARTIFACTS = "artifacts"
const val GENERATED_SOURCES = "sources"

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