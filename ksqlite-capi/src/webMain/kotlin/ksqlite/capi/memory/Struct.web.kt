package ksqlite.capi.memory

import ksqlite.capi.interop.wasm.WasmPointer
import kotlin.js.toLong

public actual open class Struct internal constructor(internal val pointer: WasmPointer) :
    StructBase() {

    actual override val address: Long
        get() = pointer.toLong()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Struct) return false

        return pointer == other.pointer

    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }

    actual override fun free() {
        TODO("Not yet implemented")
    }
}