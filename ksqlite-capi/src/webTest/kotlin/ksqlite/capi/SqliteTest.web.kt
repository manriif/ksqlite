package ksqlite.capi

internal actual val isWasm: Boolean
    get() = true

/**
 * FIXME: For now, `/tmp` seems to exist in the default 'unix' VFS so stick with it for testing
 */
internal actual fun tempTestDirectory(subdirectory: String): String = "/tmp"