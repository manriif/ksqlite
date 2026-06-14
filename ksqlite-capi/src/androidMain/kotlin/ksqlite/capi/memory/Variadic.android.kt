package ksqlite.capi.memory

/**
 * Returns the raw values of `this` [VariadicValue] array.
 */
internal fun Array<out VariadicValue<Any>?>.toJniJavaObjectArray(): Array<Any?> {
    return map { it?.value }.toTypedArray()
}