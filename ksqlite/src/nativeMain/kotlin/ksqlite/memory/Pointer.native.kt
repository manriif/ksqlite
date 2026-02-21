package ksqlite.memory

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value

/**
 * Holder for pointer to [Struct].
 */
public abstract class Pointer<Struct : CPointed> internal constructor() :
    PointerBase<CPointer<Struct>, CPointer<CPointerVar<Struct>>>() {

    override fun doAllocation(
        allocate: (CPointer<CPointerVar<Struct>>) -> Int,
        handlePointer: (CPointer<Struct>?) -> Unit
    ): Int = memScoped {
        val varPointer = allocPointerTo<Struct>()

        allocate(varPointer.ptr).also {
            handlePointer(varPointer.value)
        }
    }
}