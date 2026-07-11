package ksqlite.capi

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.EEXIST
import platform.posix.errno
import platform.posix.mkdir
import platform.windows.GetTempPathW
import platform.windows.MAX_PATH

@OptIn(ExperimentalForeignApi::class)
internal actual fun tempTestDirectory(subdirectory: String): String {
    val tmp = memScoped {
        val buffer = allocArray<UShortVar>(MAX_PATH + 1)
        val len = GetTempPathW(MAX_PATH.toUInt(), buffer)
        require(len > 0u) { "GetTempPathW failed" }
        buffer.toKString()
    }

    val path = "$tmp\\$subdirectory"

    check(mkdir(path) == 0 || errno == EEXIST) {
        "Unable to create temporary directory: $path"
    }

    return path
}