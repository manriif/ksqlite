package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer

/**
 * Returns the raw values of `this` [VariadicValue] array.
 */
internal fun Array<out VariadicValue<COpaquePointer>?>.toVariadicArguments(
    manager: () -> MemoryManager
): List<Any?> = if (isEmpty()) emptyList() else {
    map { value ->
        when (value) {
            is OfString -> manager().keyedStringPointer(value.key, value.value)
            else -> value?.value
        }
    }
}