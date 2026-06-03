@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct

public actual class sqlite3_module<AppData> internal constructor(
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>,
    module: s3_module
) : Struct(module),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VTabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, s3_module().apply {
        iVersion = version
    })

    actual override fun close(): Unit = free()
}