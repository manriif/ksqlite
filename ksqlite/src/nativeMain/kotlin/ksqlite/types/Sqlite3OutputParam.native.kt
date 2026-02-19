package ksqlite.types

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) {

    private var detachedValue: Int = initialValue
    private var attachedValue: IntVar? = null

    internal actual open val rawValue: Int
        get() = attachedValue?.value ?: detachedValue

    internal fun attach(placement: NativePlacement): CPointer<IntVar> {
        val intVar = placement.alloc(detachedValue)
        attachedValue = intVar
        return intVar.ptr
    }

    internal fun detach() {
        detachedValue = checkNotNull(attachedValue) { "Param is not attached" }.value
        attachedValue = null
    }
}