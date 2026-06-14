@file:Suppress("RemoveExplicitTypeArguments")

package ksqlite.foreign.wasm

import js.array.ReadonlyArray
import ksqlite.foreign.js.emptyJsArray
import ksqlite.foreign.js.jsArrayOf
import kotlin.js.JsAny
import kotlin.js.unsafeCast

/**
 * The "pstack" (pseudo-stack) API is a special-purpose allocator intended solely for use with
 * allocating small amounts of memory such as that needed for output pointers. It is more efficient
 * than the scoped allocation API, and covers many of the use cases for that API, but  it has a tiny
 * static memory limit (with an unspecified total size no less than 2kb).
 *
 * The pstack API is typically used like:
 *
 * ```javascript
 *
 * const pstack = sqlite3.wasm.pstack;
 * const stackPtr = pstack.pointer;
 * try {
 *   const ptr = pstack.alloc(8);
 *   // ==> pstack.pointer === ptr
 *   const otherPtr = pstack.alloc(8);
 *   // ==> pstack.pointer === otherPtr
 *   ...
 * }finally{
 *   pstack.restore(stackPtr);
 *   // ==> pstack.pointer === stackPtr
 * }
 * ```
 */
public external interface WasmPStack {

    /**
     * This property resolves to the current pstack position pointer. This value is intended only to
     * be saved for passing to restore(). Writing to this memory without first reserving it via
     * pstack.alloc() (or equivalent) leads to undefined results.
     */
    public val pointer: JsAny

    /**
     * This property resolves to the total number of bytes available in the pstack, including any
     * space which is currently allocated. This value is a compile-time constant.
     */
    public val quota: Int

    /**
     * This property resolves to the amount of space remaining in the pstack.
     */
    public val remaining: Int

    /**
     * Attempts to allocate the given number of bytes from the pstack. On success, it zeroes out a
     * block of memory of the given size, adjusts the pstack pointer, and returns a pointer to the
     * memory. On error, returns throws a WasmAllocError. The memory must eventually be released
     * using pstack.restore().
     *
     * The n may be a string accepted by wasm.sizeofIR(), and any string value not accepted by that
     * function will trigger a WasmAllocError exception.
     *
     * This method always adjusts the given value to be a multiple of 8 bytes because failing to do
     * so can lead to incorrect results when reading and writing 64-bit values from/to the WASM heap.
     * Similarly, the returned address is always 8-byte aligned.
     */
    public fun alloc(n: Int): WasmPointer

    /**
     * Attempts to allocate the given number of bytes from the pstack. On success, it zeroes out a
     * block of memory of the given size, adjusts the pstack pointer, and returns a pointer to the
     * memory. On error, returns throws a WasmAllocError. The memory must eventually be released
     * using pstack.restore().
     *
     * The n may be a string accepted by wasm.sizeofIR(), and any string value not accepted by that
     * function will trigger a WasmAllocError exception.
     *
     * This method always adjusts the given value to be a multiple of 8 bytes because failing to do
     * so can lead to incorrect results when reading and writing 64-bit values from/to the WASM heap.
     * Similarly, the returned address is always 8-byte aligned.
     */
    public fun alloc(n: String): WasmPointer

    /**
     * alloc()'s n chunks, each sz bytes, as a single memory block and returns the addresses as an
     * array of n element, each holding the address of one chunk.
     *
     * The sz argument may be a string value accepted by wasm.sizeofIR(), and any string value not
     * accepted by that function will trigger a WasmAllocError exception.
     *
     * Throws a WasmAllocError if allocation fails.
     *
     * Example:
     *
     * ```javascript
     * const [p1, p2, p3] = pstack.allocChunks(3,4);
     * ```
     */
    public fun allocChunks(
        n: Int,
        sz: Int
    ): ReadonlyArray<WasmPointer>

    /**
     * alloc()'s n chunks, each sz bytes, as a single memory block and returns the addresses as an
     * array of n element, each holding the address of one chunk.
     *
     * The sz argument may be a string value accepted by wasm.sizeofIR(), and any string value not
     * accepted by that function will trigger a WasmAllocError exception.
     *
     * Throws a WasmAllocError if allocation fails.
     *
     * Example:
     *
     * ```javascript
     * const [p1, p2, p3] = pstack.allocChunks(3,4);
     * ```
     */
    public fun allocChunks(
        n: Int,
        sz: String
    ): ReadonlyArray<WasmPointer>

    /**
     * A convenience wrapper for allocChunks() which sizes each chunk as either 8 bytes
     * (safePtrSize is truthy) or wasm.ptrSizeof (if safePtrSize is falsy).
     *
     * How it returns its result differs depending on its first argument: if it's 1, it returns a
     * single pointer value. If it's more than 1, it returns the same as allocChunks().
     *
     * When any returned pointers will refer to a 64-bit value, e.g. a double or int64, and that
     * value must be written or fetched, e.g. using wasm.poke() or wasm.peek(), it is important that
     * the pointer in question be aligned to an 8-byte boundary or else it will not be fetched or
     * written properly and will corrupt or read neighboring memory.
     *
     * However, when all pointers involved point to "small" data, it is safe to pass a falsy value
     * to save a tiny bit of memory.
     */
    public fun allocPtr(
        n: Int,
        safePtrSize: Boolean
    ): JsAny

    /**
     * Sets the current pstack position to the given pointer. Results are undefined if the passed-in
     * value did not come from pstack.pointer or if memory allocated in the space before the given
     * pointer are used after this call.
     */
    public fun restore(pstackPtr: JsAny)
}

///////////////////////////////////////////////////////////////////////////
// Type-safety
///////////////////////////////////////////////////////////////////////////

/**
 * Attempts to allocate the given number of bytes from the pstack.
 */
public fun WasmPStack.alloc(n: IR): WasmPointer = alloc(n.value)

/**
 * alloc()'s n chunks, each sz bytes, as a single memory block and returns the addresses as an
 * array of n element, each holding the address of one chunk.
 */
public fun WasmPStack.allocChunks(
    n: Int,
    sz: IR
): ReadonlyArray<WasmPointer> = allocChunks(n, sz.value)

/**
 * Allocates a pointer and set is to 0.
 */
public fun WasmPStack.allocPtr(): WasmPointer = allocPtr(1, true).unsafeCast<WasmPointer>()

/**
 * Allocates [howMany] pointers as a single chunk of memory and zeroes them out.
 */
public fun WasmPStack.allocPtr(howMany: UInt): ReadonlyArray<WasmPointer> = when (howMany) {
    0u -> emptyJsArray()
    1u -> jsArrayOf(allocPtr())
    else -> allocPtr(howMany.toInt(), true).unsafeCast<ReadonlyArray<WasmPointer>>()
}