package ksqlite.capi

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSTemporaryDirectory
import platform.posix.EEXIST
import platform.posix.errno
import platform.posix.mkdir

@OptIn(ExperimentalForeignApi::class)
internal actual fun temporaryTestDirectory(subdirectory: String): String {
    val tmp = NSTemporaryDirectory()
    val path = "$tmp/$subdirectory"

    check(mkdir(path, 0x1FFu) == 0 || errno == EEXIST) {
        "Unable to create temporary directory: $path"
    }

    return path
}