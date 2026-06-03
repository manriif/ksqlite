package ksqlite.capi.memory

import ksqlite.structs.JaccwabytStruct
import ksqlite.wasm.WasmPointer
import kotlin.js.toLong

public actual open class Struct internal constructor(
    internal val pointer: WasmPointer,
    private val struct: JaccwabytStruct? = null,
) : StructBase() {

    internal constructor(struct: JaccwabytStruct) : this(struct.pointer, struct)

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
        struct?.dispose()
    }
}