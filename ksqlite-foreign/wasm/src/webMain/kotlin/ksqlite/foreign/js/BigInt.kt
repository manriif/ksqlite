package ksqlite.foreign.js

import kotlin.js.JsBigInt
import kotlin.js.toJsBigInt
import kotlin.js.toLong

/**
 * Returns a [JsBigInt] which is the sum of `this` + [value].
 */
public operator fun JsBigInt.plus(value: Int): JsBigInt = (toLong() + value).toJsBigInt()

/**
 * Returns a [JsBigInt] which is the sum of `this` + [value].
 */
public operator fun JsBigInt.plus(value: Long): JsBigInt = (toLong() + value).toJsBigInt()