package ksqlite.capi.vtab

import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.Struct

public actual class sqlite3_index_info internal constructor(pointer: WasmPointer) :
    Struct(pointer),
    Sqlite3IndexInfo