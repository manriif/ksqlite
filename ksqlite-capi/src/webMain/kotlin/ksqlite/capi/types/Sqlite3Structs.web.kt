@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.GenericPointer
import ksqlite.capi.interop.wasm.WasmPointer

public actual class sqlite3 internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)

public actual class sqlite3_api_routines internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)

public actual class sqlite3_context internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)

public actual class sqlite3_index_info internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)

public actual class sqlite3_module internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)

public actual class sqlite3_stmt internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)

public actual class sqlite3_value internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)

public actual class sqlite3_vfs internal constructor(pointer: WasmPointer) :
    GenericPointer(pointer)