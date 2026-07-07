package ksqlite.capi.memory

import ksqlite.capi.wasm
import ksqlite.foreign.structs.StructType
import ksqlite.foreign.wasm.WasmPointer
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
}

public actual open class AllocatedStruct(private val struct: StructType) :
    Struct(struct.pointer),
    AutoCloseable {

    public actual override fun close() {
        struct.dispose()
    }
}

/**
 * For [StructType] that does not own its pointer and thus is not responsible for freeing it in
 * [StructType.dispose].
 */
public open class DeallocStruct(struct: StructType) : AllocatedStruct(struct) {

    public override fun close() {
        super.close()
        wasm.dealloc(pointer)
    }
}