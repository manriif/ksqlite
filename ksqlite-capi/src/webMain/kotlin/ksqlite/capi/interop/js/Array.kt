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
 * Returns the [array] as [JsArray].
 */
internal fun arraySize(array: ByteArray): Int {
    return array.size
}

/**
 * Workaround for IDE complaining about:
 *
 * Cannot access 'Cloneable' which is a supertype of 'Array'. Check your module classpath for
 * missing or conflicting dependencies.
 */
internal inline fun <T : Any> joinToString(
    array: Array<T>,
    separator: CharSequence,
    crossinline transform: (T) -> Any
) : String {
    return array.joinToString(separator) { item ->
        transform(item).toString()
    }
}