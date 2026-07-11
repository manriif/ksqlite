package ksqlite.capi

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

internal actual fun tempTestDirectory(subdirectory: String): String =
    InstrumentationRegistry.getInstrumentation()
        .targetContext
        .cacheDir
        .let { File(it, subdirectory) }
        .apply(File::mkdirs)
        .absolutePath