package ksqlite.capi.interop.js

import kotlin.js.JsBigInt
import kotlin.js.toJsBigInt
import kotlin.js.toLong

/**
 * Returns a [JsBigInt] which is the sum of `this` + [value].
 */
internal operator fun JsBigInt.plus(value: Int): JsBigInt {
    return (toLong() + value).toJsBigInt()
}

/**
 * Returns a [JsBigInt] which is the sum of `this` + [value].
 */
internal operator fun JsBigInt.plus(value: Long): JsBigInt {
    return (toLong() + value).toJsBigInt()
}