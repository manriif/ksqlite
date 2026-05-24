package ksqlite.capi.interop.js

import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.toJsArray

private val EmptyJsArray = toJsArray(emptyArray<JsAny?>())

/**
 * Returns the empty js array instance.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : JsAny?> emptyJsArray(): JsArray<T> {
    return EmptyJsArray as JsArray<T>
}

/**
 * Returns a [JsArray] with only a single [element].
 */
internal inline fun <reified T : JsAny?> jsArrayOf(element: T): JsArray<T> {
    return toJsArray(arrayOf(element))
}

///////////////////////////////////////////////////////////////////////////
// Workarounds  for IDE complaining about:
//
// Cannot access 'Cloneable' which is a supertype of 'Array'. Check your module classpath for
// missing or conflicting dependencies.
//
// The goal is just to keep the red underlined code in one single file
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [array] as [JsArray].
 */
internal fun <T : JsAny?> toJsArray(array: Array<T>): JsArray<T> {
    return array.toJsArray()
}

/**
 * [Array.size].
 */
internal fun arraySize(array: Array<*>): Int {
    return array.size
}

/**
 * [ByteArray.size].
 */
internal fun arraySize(array: ByteArray): Int {
    return array.size
}

/**
 * [Array.forEachIndexed].
 */
internal fun <T> arrayForEachIndexed(
    array: Array<T>,
    block: (index: Int, value: T) -> Unit
) {
    array.forEachIndexed(block)
}

/**
 * [Array.joinToString].
 */
internal inline fun <T : Any> arrayJoinToString(
    array: Array<T>,
    separator: CharSequence,
    crossinline transform: (T) -> Any
) : String {
    return array.joinToString(separator) { item ->
        transform(item).toString()
    }
}