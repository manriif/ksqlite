package ksqlite.capi.memory

import ksqlite.capi.exports
import ksqlite.foreign.js.plus
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.structs.RawStructType
import ksqlite.structs.structSize

public actual abstract class StructLayout<S : ClosableStruct> internal constructor() :
    StructLayoutBase<S>() {

    /**
     * Type of the struct.
     */
    internal abstract val type: RawStructType

    /**
     * Creates a instance of [S] wrapping [pointer].
     */
    internal abstract fun reinterpret(pointer: WasmPointer): S
}

public actual class StructArray<S : ClosableStruct> internal constructor(
    internal val pointer: WasmPointer,
    layout: StructLayout<S>,
    elements: List<S>
) : StructArrayBase<S>(layout, elements) {

    actual override fun releaseNativeArray() {
        exports.sqlite3_free(pointer)
    }
}

public actual fun <S : ClosableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>? {
    val elementSize = type.structSize
    val arraySize = elementSize * count
    val arrayPointer = exports.sqlite3_malloc(arraySize).orNull ?: return null

    val elements = List(count) { index ->
        reinterpret(arrayPointer + (index * elementSize))
            .also { initialize(it, index) }
    }

    return StructArray(arrayPointer, this, elements)
}