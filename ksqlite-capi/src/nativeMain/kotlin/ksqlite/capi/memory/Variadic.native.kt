package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer

/**
 * Returns the raw values of `this` [VariadicValue] array.
 *
 * TODO: due to Kotlin interop limitation, spreading on the returned array is not supported so all
 *  the aruments must be passed in the form of `the_function(args[0], args[1], ...)`.
 */
internal fun Array<out VariadicValue<COpaquePointer>?>.toVariadicArguments(
    manager: () -> MemoryManager
): Array<Any?> {
    return map { value ->
        when (value) {
            is OfString -> manager().keyedStringPointer(value.key, value.value)
            else -> value?.value
        }
    }.toTypedArray()
}

/**
 * Throws an exception due to unhandled variadic argument.
 */
internal fun variadicArgumentsError(): Nothing {
    throw IllegalStateException("Unexpected number of arguments for variadic function call")
}