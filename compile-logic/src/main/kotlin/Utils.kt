/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileSystemOperations
import java.io.File
import kotlin.io.path.createTempDirectory

///////////////////////////////////////////////////////////////////////////
// Files
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