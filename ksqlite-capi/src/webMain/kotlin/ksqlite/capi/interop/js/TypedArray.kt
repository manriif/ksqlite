package ksqlite.capi.interop.js

import kotlin.js.JsAny
import kotlin.js.JsBigInt
import kotlin.js.JsNumber
import kotlin.js.definedExternally

/**
 * A [TypedArray](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray)
 * object describes an array-like view of an underlying binary data buffer.
 */
internal sealed external class TypedArray<T: JsAny, R: TypedArray<T, R>> : JsAny {

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
     * Returns a section of an array.
     * @param start The beginning of the specified portion of the array.
     * @param end The end of the specified portion of the array. This is exclusive of the element at the index 'end'.
     */
    fun slice(
        start: Int = definedExternally,
        end: Int = definedExternally,
    ): R
}

///////////////////////////////////////////////////////////////////////////
// Subclasses
///////////////////////////////////////////////////////////////////////////

internal external class Int8Array : TypedArray<JsNumber, Int8Array>
internal external class Uint8Array : TypedArray<JsNumber, Uint8Array>
internal external class Int16Array : TypedArray<JsNumber, Int16Array>
internal external class Uint16Array : TypedArray<JsNumber, Uint16Array>
internal external class Int32Array : TypedArray<JsNumber, Int32Array>
internal external class Uint32Array : TypedArray<JsNumber, Uint32Array>
internal external class BigInt64Array : TypedArray<JsBigInt, BigInt64Array>
internal external class BigUint64Array : TypedArray<JsBigInt, BigUint64Array>