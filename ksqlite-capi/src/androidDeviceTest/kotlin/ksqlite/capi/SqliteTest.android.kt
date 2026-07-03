package ksqlite.capi

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

internal actual fun temporaryTestDirectory(subdirectory: String): String =
    InstrumentationRegistry.getInstrumentation()
        .targetContext
        .cacheDir
        .let { File(it, subdirectory) }
        .apply(File::mkdirs)
        .absolutePath