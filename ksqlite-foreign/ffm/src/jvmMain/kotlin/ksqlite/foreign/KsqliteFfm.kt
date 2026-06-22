package ksqlite.foreign

import java.io.File
import java.nio.file.Files

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
 * Loads the Kotlin SQLite library.
 */
@Suppress("UnsafeDynamicallyLoadedCode")
public fun ksqliteLoadLibrary() {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    val libPath = ksqliteLibPath(osName, osArch)

    val libraryStream = Thread
        .currentThread()
        .contextClassLoader
        .getResourceAsStream(libPath)
        ?: error("Native library not found: $libPath")

    val tempFile = Files
        .createTempFile("lib${KSQLITE_NATIVE_LIB_NAME}", libPath.substringAfterLast('.'))
        .toFile()
        .also(File::deleteOnExit)

    val _ = libraryStream.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    System.load(tempFile.absolutePath)
}