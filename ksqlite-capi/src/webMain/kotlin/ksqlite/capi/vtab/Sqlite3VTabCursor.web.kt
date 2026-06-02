package ksqlite.capi.vtab

import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.StructPointer

public actual class sqlite3_vtab_cursor internal constructor(pointer: WasmPointer) :
    StructPointer(pointer)