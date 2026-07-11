package ksqlite.capi.memory

/**
 * Returns the raw values of `this` [VariadicValue] array.
 */
internal fun Array<out VariadicValue<Any>?>.toJniJavaObjectArray(): Array<Any?> = map { value ->
    when (value) {
        is OfUInt -> value.value.toInt()
        else -> value?.value
    }
}.toTypedArray()