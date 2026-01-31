package ksqlite

internal actual object NativeLibraryLoader {

    actual fun loadLibrary() {
        System.loadLibrary(KSQLITE_NATIVE_LIB)
    }
}