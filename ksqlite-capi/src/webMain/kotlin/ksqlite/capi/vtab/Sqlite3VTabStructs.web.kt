@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.StructPointer

public actual class sqlite3_index_info internal constructor(pointer: WasmPointer) :
    StructPointer(pointer),
    Sqlite3IndexInfo

public actual class sqlite3_module internal constructor(pointer: WasmPointer) :
    StructPointer(pointer)

public actual class sqlite3_vtab internal constructor(pointer: WasmPointer) :
    StructPointer(pointer)

public actual class sqlite3_vtab_cursor internal constructor(pointer: WasmPointer) :
    StructPointer(pointer)
