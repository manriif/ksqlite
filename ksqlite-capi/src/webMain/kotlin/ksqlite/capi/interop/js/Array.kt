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
internal inline fun <reified T: JsAny?> jsArrayOf(element: T): JsArray<T> {
    return toJsArray(arrayOf(element))
}

/**
 * Workaround for IDE complaining about:
 *
 * Cannot access 'Cloneable' which is a supertype of 'Array'. Check your module classpath for
 * missing or conflicting dependencies.
 */
internal fun <T : JsAny?> toJsArray(array: Array<T>): JsArray<T> {
    return array.toJsArray()
}