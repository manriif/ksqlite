@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.foreign.wasm.WasmPointer
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct

public actual class sqlite3 internal constructor(pointer: WasmPointer) :
    Struct(pointer),
    MemoryScope

public actual class sqlite3_backup internal constructor(pointer: WasmPointer) :
    Struct(pointer)

public actual class sqlite3_blob internal constructor(pointer: WasmPointer) :
    Struct(pointer)

public actual class sqlite3_context internal constructor(pointer: WasmPointer) :
    Struct(pointer)

public actual class sqlite3_snapshot internal constructor(pointer: WasmPointer) :
    Struct(pointer)

public actual class sqlite3_stmt internal constructor(pointer: WasmPointer) :
    Struct(pointer),
    MemoryScope

public actual class sqlite3_value internal constructor(pointer: WasmPointer) :
    Struct(pointer)

public actual class sqlite3_vfs internal constructor(pointer: WasmPointer) :
    Struct(pointer)