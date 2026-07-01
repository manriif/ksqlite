@file:Suppress("ClassName")

package ksqlite.capi

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct
import ksqlite.foreign.wasm.WasmMemory
import ksqlite.foreign.wasm.WasmPointer

public actual class sqlite3 internal constructor(pointer: WasmPointer) :
    Struct(pointer),
    MemoryScope {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3>() {

        override fun WasmMemory.create(pointer: WasmPointer): sqlite3 = sqlite3(pointer)
    }
}

public actual class sqlite3_backup internal constructor(pointer: WasmPointer) :
    Struct(pointer)

public actual class sqlite3_blob internal constructor(pointer: WasmPointer) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_blob>() {

        override fun WasmMemory.create(pointer: WasmPointer): sqlite3_blob = sqlite3_blob(pointer)
    }
}

public actual class sqlite3_context internal constructor(pointer: WasmPointer) :
    Struct(pointer)

public actual class sqlite3_snapshot internal constructor(pointer: WasmPointer) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_snapshot>() {

        override fun WasmMemory.create(pointer: WasmPointer): sqlite3_snapshot =
            sqlite3_snapshot(pointer)
    }
}

public actual class sqlite3_stmt internal constructor(pointer: WasmPointer) :
    Struct(pointer),
    MemoryScope {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_stmt>() {

        override fun WasmMemory.create(pointer: WasmPointer): sqlite3_stmt = sqlite3_stmt(pointer)
    }
}

public actual class sqlite3_value internal constructor(pointer: WasmPointer) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_value>() {

        override fun WasmMemory.create(pointer: WasmPointer): sqlite3_value = sqlite3_value(pointer)
    }
}