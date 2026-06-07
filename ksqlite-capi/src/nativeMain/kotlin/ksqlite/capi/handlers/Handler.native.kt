package ksqlite.capi.handlers

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.stableRefDataHolder

/**
 * Returns [pointer] to a [CFunction] only if [data] is not `null`.
 */
internal fun <Fun : CFunction<*>, Pointer : CPointer<Fun>> callbackHandler(
    data: Any?,
    pointer: Pointer
): Pointer? {
    if (data == null) {
        return null
    }

    return pointer
}

/**
 * Returns [block]'s result, invoked with [Data] and optional application data obtained from a
 * previously referenced [refPointer].
 *
 * AppData type is erased to reduce complexity.
 */
internal inline fun <reified Data : Any, Result> handle(
    refPointer: COpaquePointer?,
    block: (data: Data, appData: Any?) -> Result
): Result = stableRefDataHolder<Data, Any?>(refPointer).run {
    block(data, appData)
}