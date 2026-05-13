package ksqlite

import java.nio.file.Files

///////////////////////////////////////////////////////////////////////////
// Library
///////////////////////////////////////////////////////////////////////////

private fun String.isX64(): Boolean {
    return contains("amd64") || contains("x86_64")
}

private fun String.isArm64(): Boolean {
    return contains("aarch64") || contains("arm64")
}

/**
 * Loads the Kotlin SQLite library.
 */
@Suppress("UnsafeDynamicallyLoadedCode")
public fun ksqliteLoadLibrary() {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    val libPath = when {
        osName.contains("mac") || osName.contains("darwin") -> when {
            //osArch.isX64() -> KSQLITE_NATIVE_LIB_MACOS_X86_64_PATH
            osArch.isArm64() -> KSQLITE_NATIVE_LIB_MACOS_AARCH64_PATH
            else -> null
        }

        osName.contains("linux") -> when {
            //osArch.isX64() -> KSQLITE_NATIVE_LIB_LINUX_X86_64_PATH
            //osArch.isArm64() -> KSQLITE_NATIVE_LIB_LINUX_AARCH64_PATH
            else -> null
        }

        osName.contains("windows") -> when {
            //osArch.isX64() -> KSQLITE_NATIVE_LIB_WINDOWS_X86_64_PATH
            //osArch.isArm64() -> KSQLITE_NATIVE_LIB_WINDOWS_AARCH64_PATH
            else -> null
        }

        else -> null
    }

    if (libPath == null) {
        error("Unsupported platform: $osName $osArch")
    }

    val libraryStream = Thread
        .currentThread()
        .contextClassLoader
        .getResourceAsStream(libPath)
        ?: error("Native library not found: $libPath")

    val tempFile = Files
        .createTempFile("lib${KSQLITE_NATIVE_LIB_NAME}", libPath.substringAfterLast('.'))
        .toFile()
        .apply { deleteOnExit() }

    val _ = libraryStream.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    System.load(tempFile.absolutePath)
}