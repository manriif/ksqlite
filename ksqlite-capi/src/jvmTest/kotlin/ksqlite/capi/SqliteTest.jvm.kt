package ksqlite.capi

import java.io.File

internal actual fun temporaryTestDirectory(subdirectory: String): String =
    File(System.getProperty("java.io.tmpdir"), subdirectory)
        .apply(File::mkdirs)
        .absolutePath