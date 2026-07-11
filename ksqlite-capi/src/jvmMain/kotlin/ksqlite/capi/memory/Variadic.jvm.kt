package ksqlite.capi.memory

import ksqlite.capi.memory.VariadicValue.OfPointer
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Invokes a function accepting a variadic parameter.
 * String parameter are allocated within [manager].
 */
internal inline fun <Result> invokeVariadic(
    values: Array<out VariadicValue<MemorySegment>?>,
    manager: () -> MemoryManager,
    invoke: (layouts: Array<out MemoryLayout>, arguments: Array<out Any>) -> Result
): Result {
    val layouts = Array(values.size) { index ->
        when (values[index]) {
            is OfInt, is OfUInt -> ValueLayout.JAVA_INT
            is OfLong -> ValueLayout.JAVA_LONG
            is OfPointer, is OfString, null -> ValueLayout.ADDRESS
        }
    }

    val arguments = Array(values.size) { index ->
        when (val value = values[index]) {
            null -> NullPtr
            is OfUInt -> value.value.toInt()
            !is OfString -> value.value
            else -> manager().keyedStringPointer(value.key, value.value)
        }
    }

    return invoke(layouts, arguments)
}