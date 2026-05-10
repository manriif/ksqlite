package ksqlite.capi.interop.js

import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsBigInt
import kotlin.js.JsNumber
import kotlin.js.definedExternally

/**
 * A [TypedArray](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray)
 * object describes an array-like view of an underlying binary data buffer.
 */
internal sealed external class TypedArray<T : JsAny, R : TypedArray<T, R>> : JsAny {

    /**
     * Read-only. The length of the ArrayBuffer (in bytes).
     */
    val byteLength: Int

    /**
     * Returns the item located at the specified index.
     *
     * @param index The zero-based index of the desired code unit. A negative index will count back
     * from the last item.
     */
    fun at(index: Int): T

    /**
     * Returns the item located at the specified index.
     *
     * @param index The zero-based index of the desired code unit. A negative index will count back
     * from the last item.
     */
    operator fun get(index: Int): T = definedExternally

    /**
     * The set() method of TypedArray instances stores multiple values in the typed array, reading
     * input values from a specified array.
     */
    fun set(
        array: TypedArray<T, R>,
        offset: Int = definedExternally,
    )

    /**
     * Returns a section of an array.
     * @param start The beginning of the specified portion of the array.
     * @param end The end of the specified portion of the array. This is exclusive of the element at the index 'end'.
     */
    fun slice(
        start: Int = definedExternally,
        end: Int = definedExternally,
    ): R

    /**
     * The subarray() method of TypedArray instances returns a new typed array on the same
     * ArrayBuffer store and with the same element types as for this typed array.
     *
     * @param start The beginning of the specified portion of the array.
     * @param end The end of the specified portion of the array. This is exclusive of the element at
     * the index 'end'.
     */
    fun subarray(
        start: Int = definedExternally,
        end: Int = definedExternally,
    ): R
}

///////////////////////////////////////////////////////////////////////////
// Subclasses
///////////////////////////////////////////////////////////////////////////

internal external class Int8Array(array: JsArray<JsNumber>) : TypedArray<JsNumber, Int8Array>
internal external class Uint8Array(array: JsArray<JsNumber>) : TypedArray<JsNumber, Uint8Array>
internal external class Int16Array(array: JsArray<JsNumber>) : TypedArray<JsNumber, Int16Array>
internal external class Uint16Array(array: JsArray<JsNumber>) : TypedArray<JsNumber, Uint16Array>
internal external class Int32Array(array: JsArray<JsNumber>) : TypedArray<JsNumber, Int32Array>
internal external class Uint32Array(array: JsArray<JsNumber>) : TypedArray<JsNumber, Uint32Array>

internal external class BigInt64Array(array: JsArray<JsBigInt>) :
    TypedArray<JsBigInt, BigInt64Array>

internal external class BigUint64Array(array: JsArray<JsBigInt>) :
    TypedArray<JsBigInt, BigUint64Array>

///////////////////////////////////////////////////////////////////////////
// Conversions & util
///////////////////////////////////////////////////////////////////////////

/**
 * Converts [array] to [Int8Array].
 */
internal expect inline fun toInt8Array(array: ByteArray): Int8Array

/**
 * Copies `this` [Int8Array] bytes to [target] starting at [targetOffset].
 */
internal expect inline fun Int8Array.copyTo(target: ByteArray, targetOffset: Int)

/**
 * Copies bytes from [source] at [sourceOffset] to `this` [Int8Array].
 */
internal expect inline fun Int8Array.copyFrom(source: ByteArray, sourceOffset: Int)