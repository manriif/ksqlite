@file:Suppress("NOTHING_TO_INLINE")

package ksqlite

import java.nio.file.Files
import kotlin.time.measureTimedValue

public fun main() {
    ksqliteLoadLibrary()

    val results = (0..<1000000).map {
        measureTimedValue {
            sqlite3.sqlite3_libversion().getString(0)
        }
    }

    println("Result = ${results.sumOf { it.duration.inWholeNanoseconds } / results.size}")
}

public inline fun String.isX64(): Boolean {
    return contains("amd64") || contains("x86_64")
}

public inline fun String.isArm64(): Boolean {
    return contains("aarch64") || contains("arm64")
}

@Suppress("UnsafeDynamicallyLoadedCode")
public fun ksqliteLoadLibrary() {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    val libPath = when {
        osName.contains("mac") || osName.contains("darwin") -> when {
            osArch.isX64() -> KSQLITE_NATIVE_LIB_MACOS_X86_64_PATH
            //osArch.isArm64() -> KSQLITE_NATIVE_LIB_MACOS_AARCH64_PATH
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
        .createTempFile("lib$KSQLITE_NATIVE_LIB_NAME", libPath.substringAfterLast('.'))
        .toFile()
        .apply { deleteOnExit() }

    // Extract library to temp file
    libraryStream.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    // Load the library
    System.load(tempFile.absolutePath)
}