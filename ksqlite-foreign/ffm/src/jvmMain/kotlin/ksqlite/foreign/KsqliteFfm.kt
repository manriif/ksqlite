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
package ksqlite.foreign

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.absolutePathString

/**
 * Whether this os represents Linux.
 */
internal fun String.isLinux(): Boolean = contains("linux")

/**
 * Whether this os represents macOS.
 */
internal fun String.isMacOs(): Boolean = contains("mac") || contains("osx")

/**
 * Whether this os represents Windows.
 */
internal fun String.isWindows(): Boolean = contains("windows")

/**
 * Whether this architecture represents AMD64.
 */
internal fun String.isAmd64(): Boolean = contains("amd64") || contains("x86_64")

/**
 * Whether this architecture represents ARM64.
 */
internal fun String.isArm64(): Boolean = contains("arm64") || contains("aarch64")

/**
 * Returns the hex-formated SHA-256 hash of this [Path].
 */
private fun Path.sha256Hex(): String {
    val digest = MessageDigest.getInstance("SHA-256")

    Files.newInputStream(this).use { input ->
        val buffer = ByteArray(8192)

        while (true) {
            val read = input.read(buffer)

            if (read < 0) {
                break
            }

            digest.update(buffer, 0, read)
        }
    }

    return digest.digest().joinToString("") { "%02x".format(it) }
}

/**
 * Stable, per-user cache directory for extracted native libraries.
 */
private fun ksqliteCacheDir(osName: String): Path {
    val home = System.getProperty("user.home")

    val base = when {
        osName.isWindows() -> System.getenv("LOCALAPPDATA") ?: "$home/AppData/Local"
        osName.isMacOs() -> "$home/Library/Caches"
        osName.isLinux() -> System.getenv("XDG_CACHE_HOME") ?: "$home/.cache"
        else -> error("Unsupported operating system: $osName")
    }

    return Path.of(base, "ksqlite", "native")
}

/**
 * Loads the Kotlin SQLite library.
 */
@Suppress("UnsafeDynamicallyLoadedCode")
public fun ksqliteLoadLibrary() {
    val loader = Thread.currentThread().contextClassLoader
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    val libPath = ksqliteLibPath(osName, osArch)
    val libHashPath = "$libPath.sha256"

    val libHash = loader.getResourceAsStream(libHashPath)
        ?.bufferedReader()
        ?.readText()
        ?: error("Hash for native library was not found: $libHashPath")

    val fileName = libPath.substringAfterLast('/')
    val cacheDir = ksqliteCacheDir(osName)
    Files.createDirectories(cacheDir)
    val targetFile = cacheDir.resolve("$fileName.$libHash")

    val isCacheValid = Files.exists(targetFile) && runCatching { targetFile.sha256Hex() == libHash }
        .getOrDefault(false)

    if (!isCacheValid) {
        val stagingFile = Files.createTempFile(
            cacheDir,
            "lib${KSQLITE_NATIVE_LIB_NAME}-staging",
            libPath.substringAfterLast('.'),
        )

        try {
            val libraryStream = loader.getResourceAsStream(libPath)
                ?: error("Native library not found: $libPath")

            libraryStream.use { input ->
                Files.newOutputStream(stagingFile).use { output ->
                    input.copyTo(output)
                }
            }

            val stagingHash = stagingFile.sha256Hex()

            check(stagingHash == libHash) {
                "Extracted native library hash mismatch for $libPath: " +
                        "expected $libHash but got $stagingHash"
            }

            Files.move(
                stagingFile,
                targetFile,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } finally {
            Files.deleteIfExists(stagingFile)
        }
    }

    System.load(targetFile.absolutePathString())
}