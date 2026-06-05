@file:Suppress("ClassName", "ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.nativeVTabModuleInit

public actual class sqlite3_module<AppData> private constructor(module: s3_module) :
    Struct(module),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VTabModuleCallbacks<AppData, *, *>
    ) : this(s3_module().apply {
        iVersion = version

        var mask = 0 // TODO

        nativeVTabModuleInit(
            module = pointer,
            methodMask = mask,
            eponymous = callbacks.moduleKind == Sqlite3ModuleKind.Eponymous,
            callbacks = VTabModuleHandler()
        )
    })

    actual override fun close(): Unit = free()
}