package ksqlite.js

import js.array.ReadonlyArray
import js.array.jsArrayOf
import js.typedarrays.Int8Array
import js.typedarrays.internal.castOrConvertToByteArray
import js.typedarrays.toByteArray
import js.typedarrays.toInt8Array
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.toJsArray
import kotlin.js.unsafeCast

private val EmptyJsArray: ReadonlyArray<JsAny> = jsArrayOf()

/**
 * Returns the empty js array instance.
 */
internal fun <T : JsAny?> emptyJsArray(): ReadonlyArray<T> =
    EmptyJsArray.unsafeCast<ReadonlyArray<T>>()

/**
 * Returns a [JsArray] with only a single [element].
 */
public inline fun <reified T : JsAny?> jsArrayOf(element: T): ReadonlyArray<T> =
    jsArrayOf(element)

///////////////////////////////////////////////////////////////////////////
// Below are workarounds for IDE complaining about:
//
// Cannot access 'Cloneable' which is a supertype of 'Array'. Check your module classpath for
// missing or conflicting dependencies.
//
// The goal is just to keep the red underlined code in one single file
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [array] as [JsArray].
 */
public inline fun <T : JsAny?> toJsArray(array: Array<T>): ReadonlyArray<T> {
    return array.toJsArray().unsafeCast<ReadonlyArray<T>>()
}

/**
 * [Array.size].
 */
public inline fun arraySize(array: Array<*>): Int {
    return array.size
}

/**
 * [ByteArray.size].
 */
public inline fun arraySize(array: ByteArray): Int {
    return array.size
}

/**
 * [Array.forEachIndexed].
 */
public inline fun <T> arrayForEachIndexed(
    array: Array<T>,
    block: (index: Int, value: T) -> Unit
) {
    array.forEachIndexed(block)
}

/**
 * [Array.joinToString].
 */
public inline fun <T : Any> arrayJoinToString(
    array: Array<T>,
    separator: CharSequence,
    crossinline transform: (T) -> Any
) : String = array.joinToString(separator) { item ->
    transform(item).toString()
}

/**
 * Converts [array] to [ByteArray].
 */
public inline fun toByteArray(array: Int8Array<*>): ByteArray = array.toByteArray()

/**
 * Converts [array] to [Int8Array].
 */
public inline fun toInt8Array(array: ByteArray): Int8Array<*> = array.toInt8Array()

/**
 * Converts [array] to [Int8Array] retaining only first [size] bytes.
 */
public expect fun toInt8Array(array: ByteArray, size: Int): Int8Array<*>

/**
 * Copies `this` [Int8Array] bytes to [target] starting at [targetOffset].
 */
public expect fun Int8Array<*>.copyTo(target: ByteArray, targetOffset: Int)

/**
 * Copies bytes from [source] at [sourceOffset] to `this` [Int8Array].
 */
public expect fun Int8Array<*>.copyFrom(source: ByteArray, sourceOffset: Int)