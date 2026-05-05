package ksqlite.capi.utils

import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.toJsArray
import kotlin.js.unsafeCast

private val EmptyJsArray = toJsArray(emptyArray())

/**
 * Returns the empty js array instance.
 */
internal fun <T : JsAny?> emptyJsArray(): JsArray<T> {
    return EmptyJsArray.unsafeCast()
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