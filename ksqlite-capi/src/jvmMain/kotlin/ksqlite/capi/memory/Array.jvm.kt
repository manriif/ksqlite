package ksqlite.capi.memory

import ksqlite.foreign.sqlite3
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment

public actual abstract class StructLayout<S : ClosableStruct> internal constructor() :
    StructLayoutBase<S>() {

    /**
     * Layout of the struct.
     */
    internal abstract val layout: MemoryLayout

    /**
     * Creates a instance of [S] wrapping [pointer].
     */
    internal abstract fun reinterpret(pointer: MemorySegment): S
}

public actual class StructArray<S : ClosableStruct> internal constructor(
    internal val pointer: MemorySegment,
    layout: StructLayout<S>,
    elements: List<S>
) : StructArrayBase<S>(layout, elements) {

    actual override fun releaseNativeArray() {
        sqlite3.sqlite3_free(pointer)
    }
}

public actual fun <S : ClosableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>? {
    val elementSize = layout.byteSize()
    val arraySize = elementSize * count.toLong()
    val arrayPointer = sqlite3.sqlite3_malloc64(arraySize).orNull ?: return null
    val resizedArrayPointer = arrayPointer.reinterpret(arraySize)

    val elements = List(count) { index ->
        reinterpret(resizedArrayPointer.asSlice(index * elementSize, layout))
            .also { initialize(it, index) }
    }

    return StructArray(arrayPointer, this, elements)
}