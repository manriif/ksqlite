package ksqlite.capi.vtab

import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.Struct

public actual class sqlite3_vtab internal constructor(pointer: WasmPointer) :
    Struct(pointer)