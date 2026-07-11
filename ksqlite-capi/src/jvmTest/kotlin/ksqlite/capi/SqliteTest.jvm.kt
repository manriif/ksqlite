package ksqlite.capi

import java.io.File

internal actual fun tempTestDirectory(subdirectory: String): String =
    File(System.getProperty("java.io.tmpdir"), subdirectory)
        .apply(File::mkdirs)
        .absolutePath