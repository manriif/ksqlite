package ksqlite.capi.memory

import ksqlite.foreign.JniPointer
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_malloc
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
    internal abstract fun reinterpret(pointer: JniPointer): S
}

public actual class StructArray<S : ClosableStruct> internal constructor(
    internal val pointer: JniPointer,
    layout: StructLayout<S>,
    elements: List<S>
) : StructArrayBase<S>(layout, elements) {

    actual override fun releaseNativeArray() {
        sqlite3_free(pointer)
    }
}

public actual fun <S : ClosableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>? {
    val elementSize = type.structSize
    val arraySize = elementSize * count
    val arrayPointer = sqlite3_malloc(arraySize).orNull ?: return null

    val elements = List(count) { index ->
        reinterpret(arrayPointer + index * elementSize)
            .also { initialize(it, index) }
    }

    return StructArray(arrayPointer, this, elements)
}