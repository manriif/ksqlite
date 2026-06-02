@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.memory.Struct

public actual class sqlite3_module<AppData>
internal actual constructor(
    version: Int,
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>
) : Struct(NullPtr),
    AutoCloseable {

    actual override fun close(): Unit = free()
}