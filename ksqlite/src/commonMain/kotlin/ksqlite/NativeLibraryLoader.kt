package ksqlite

/**
 * Helper class to load native SQLite library based on the host platform.
 */
internal expect object NativeLibraryLoader {

    /**
     * Loads the SQLite library.
     */
    fun loadLibrary()
}