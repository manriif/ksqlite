package ksqlite.capi.handlers

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.refData
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Returns `this` [Pointer] to a [CFunction] only if [data] is not `null`.
 */
internal fun <Fun : CFunction<*>, Pointer : CPointer<Fun>> Pointer.handle(data: Any?): Pointer? {
    if (data == null) {
        return null
    }

    return this
}

/**
 * Returns [block]'s result, invoked with [Data] and optional userData obtained from a previously
 * referenced [refPointer].
 */
internal inline fun <Data : Any, Result> handler(
    refPointer: COpaquePointer?,
    block: (data: Data, userData: sqlite3_mutable_pointer?) -> Result
): Result {
    val (data, userData) = refData<Data>(refPointer)
    return block(data, userData)
}