@file:Suppress("RemoveExplicitTypeArguments", "SpellCheckingInspection")

package ksqlite.foreign.wasm

import js.array.ReadonlyArray
import js.typedarrays.Int16Array
import js.typedarrays.Int32Array
import js.typedarrays.Int8Array
import js.typedarrays.TypedArray
import js.typedarrays.Uint16Array
import js.typedarrays.Uint32Array
import js.typedarrays.Uint8Array
import ksqlite.foreign.js.emptyJsArray
import ksqlite.foreign.js.jsArrayOf
import kotlin.js.JsAny
import kotlin.js.JsBigInt
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.definedExternally
import kotlin.js.toJsString
import kotlin.js.unsafeCast

/**
 * Just like in C, WASM offers a memory "heap," and transfering values between JS and WASM often
 * requires manipulation of that memory, including low-level allocation and deallocation of it. The
 * following subsections describe the various memory management APIs.
 *
 * [MemoryManagement](https://sqlite.org/wasm/doc/trunk/api-wasm.md#memory-management)
 */
public  external interface WasmMemory {

    /**
     * [WasmPStack] instance.
     */
    public val pstack: WasmPStack

    /**
     * [WasmPtr] instance.
     */
    public val ptr: WasmPtr

    ///////////////////////////////////////////////////////////////////////////
    // Low-level Management
    //
    // The lowest-level memory management works like C's standard malloc(), realloc(), and free(),
    // the one difference being that exceptions are used for reporting out-of-memory conditions.
    // In order to avoid certain API misuses caused by mixing different allocators, the canonical
    // sqlite3.js builds wrap sqlite3_malloc(), sqlite3_realloc(), and sqlite3_free() instead of
    // malloc(), realloc(), and free(), but the semantics of both pairs are effectively identical.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Allocates n bytes of memory from the WASM heap and returns the address of the first byte in
     * the block. alloc() throws a WasmAllocError if allocation fails. If non-thowing allocation is
     * required, use alloc.impl(n), which returns a WASM NULL pointer (the integer 0) if allocation
     * fails.
     *
     * Note that memory allocated this way is not automatically zeroed out. In practice that has not
     * proven to be a problem (in JS, at least) because memory is only explicitly allocated when it
     * has a specific use and will be populated by the code which allocates it.
     */
    public val alloc: WasmAlloc

    /**
     * Semantically equivalent to realloc(3) or sqlite3_realloc(), this routine reallocates memory
     * allocated via this routine or alloc(). Its first argument is either 0 or a pointer returned
     * by this routine or alloc(). Its second argument is the number of bytes to (re)allocate, or 0
     * to free the memory specified in the first argument. On allocation error, realloc() throws a
     * WasmAllocError, whereas realloc.impl() will return 0 on allocation error.
     *
     * Be aware that reassigning the return value of realloc.impl() is poor practice and can lead to
     * leaks of heap memory, as in this contrived example:
     *
     * ```javascript
     * let m = wasm.realloc.impl(0, 10); // allocate 10 bytes
     * m = wasm.realloc.impl(m, 20); // grow m to 20 bytes
     * ```
     *
     * If that reallocation fails, it will return 0, overwriting m and effectively leaking the first
     * allocation. Always use an intermediary value for such cases:
     *
     * ```javascript
     * let m2 = wasm.realloc.impl(m, 20);
     * if( m2 ) m = m2;
     * else { ... error ... }
     * ```
     */
    public val realloc: WasmRealloc

    /**
     * Uses alloc() to allocate enough memory for the byte-length of the given JS string, plus 1
     * (for a NUL terminator), copies the given JS string to that memory using jstrcpy(),
     * NUL-terminates it, and returns the pointer to that C-string. Ownership of the pointer is
     * transfered to the caller, who must eventually pass the pointer to dealloc() to free it.
     *
     * If passed a truthy 2nd argument then its return semantics change: it returns [ptr,n], where
     * ptr is the C-string's pointer and n is its cstrlen().
     */
    public fun allocCString(
        jsString: JsString,
        returnWithLength: Boolean
    ): JsAny

    /**
     * Allocates one or more pointers as a single chunk of memory and zeroes them out.
     *
     * The first argument is the number of pointers to allocate. The second specifies whether they
     * should use a "safe" pointer size (8 bytes) or whether they may use the default pointer size
     * (typically 4 but also possibly 8).
     *
     * How the result is returned depends on its first argument: if passed 1, it returns the
     * allocated memory address. If passed more than one then an array of pointer addresses is
     * returned, which can optionally be used with "destructuring assignment" like this:
     *
     * ```javascript
     * const [p1, p2, p3] = allocPtr(3);
     * ```
     *
     * ACHTUNG: when freeing the memory, pass only the first result value to dealloc(). The others
     * are part of the same memory chunk and must not be freed separately.
     *
     * The reason for the 2nd argument is...
     *
     * When one of the returned pointers will refer to a 64-bit value, e.g. a double or int64, and
     * that value must be written or fetched, e.g. using poke() or peek(), it is important that the
     * pointer in question be aligned to an 8-byte boundary or else it will not be fetched or
     * written properly and will corrupt or read neighboring memory. It is only safe to pass false
     * when the client code is certain that it will only get/fetch 4-byte values (or smaller).
     */
    public fun allocPtr(
        howMany: Int,
        safePtrSize: Boolean
    ): JsAny

    /**
     * Frees memory returned by alloc(). Results are undefined if it is passed any value other than
     * a value returned by alloc() or null/undefined/0 (all of which are no-ops).
     */
    public fun dealloc(pointer: WasmPointer)

    /**
     * For the given IR-like string in the set ('i8', 'i16', 'i32', 'f32', 'float', 'i64', 'f64',
     * 'double', '*'), or any string value ending in '*', returns the sizeof for that value
     * (wasm.ptrSizeof in the latter case). For any other value, it returns the undefined value.
     *
     * Some allocation routines use this to enable callers to pass them an IR value instead of an
     * integer.
     */
    public fun sizeofIR(ir: JsString): Int

    ///////////////////////////////////////////////////////////////////////////
    // "Scoped" Allocation Management
    //
    // It is often convenient to manage allocations in such a way that all allocations made in a
    // particular block are "automatically" cleaned up when that block exits. This API provides
    // "scoped" allocation routines which work this way.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Opens a new "scope" for allocations. All allocations made via the scopedAllocXyz() APIs will
     * store their results into the current (most recently pushed) allocation scope for later
     * cleanup. The returned value must be retained for passing to scopedAllocPop().
     *
     * Any number of scopes may be active at once, but they must be popped in reverse order of their
     * creation. i.e. they must nest in a manner equivalent to C-style scopes.
     *
     * Warnings:
     *
     * - All the other scopedAllocXyz() routines will throw if no scope is active.
     * - It is never legal to pass the result of a scoped allocation to dealloc(), and doing so will
     * cause a double-free when the scope is closed with scopedAllocPop().
     *
     * This function and its relatives have only a single intended usage pattern:
     *
     * ```javascript
     * const scope = wasm.scopedAllocPush();
     * try {
     *   ... use scopedAllocXyz() routines ...
     *   // It is perfectly legal to use non-scoped allocations here,
     *   // they just won't be cleaned up when...
     * }finally{
     *   wasm.scopedAllocPop(scope);
     * }
     *```
     */
    public fun scopedAllocPush(): JsAny

    /**
     * Works just like alloc(n) but stores the result of the allocation in the current scope.
     *
     * This function's read-only level property resolves to the current allocation scope depth.
     */
    public fun scopedAlloc(n: Int): WasmPointer

    /**
     * Calls scopedAllocPush(), calls the given callback, and then calls scopedAllocPop(),
     * propagating any exception from the callback or returning its result. This is essentially a
     * convenience form of:
     *
     * ```javascript
     * const scope = wasm.scopedAllocPush();
     * try { return callback() }
     * finally{ wasm.scopedAllocPop(scope) }
     * ```
     */
    public fun <R: JsAny> scopedAllocCall(callback: () -> R): R

    /**
     * Uses alloc() to allocate enough memory for the byte-length of the given JS string, plus 1
     * (for a NUL terminator), copies the given JS string to that memory using jstrcpy(),
     * NUL-terminates it, and returns the pointer to that C-string. Ownership of the pointer is
     * transfered to the caller, who must eventually pass the pointer to dealloc() to free it.
     *
     * If passed a truthy 2nd argument then its return semantics change: it returns [ptr,n], where
     * ptr is the C-string's pointer and n is its cstrlen().
     */
    public fun scopedAllocCString(
        jsString: JsString,
        returnWithLength: Boolean
    ): JsAny

    /**
     * Works just like allocPtr() but stores the result of the allocation in the current scope.
     */
    public fun scopedAllocPtr(
        howMany: Int,
        safePtrSize: Boolean
    ): JsAny

    /**
     * Given a value returned from scopedAllocPush(), this "pops" that allocation scope and frees
     * all memory allocated in that scope by the scopedAllocXyz() family of APIs.
     *
     * It is technically legal to call this without any argument, but passing an argument allows the
     * allocator to perform sanity checking to ensure that scopes are pushed and popped in the
     * proper order (it throws if they are not). Failing to pass an argument is not illegal but will
     * make that sanity check impossible.
     */
    public fun scopedAllocPop(scope: JsAny)

    ///////////////////////////////////////////////////////////////////////////
    // Getting/Setting Memory Values
    //
    // The WASM memory heap is exposed to JS as a byte array of memory which is made to appear
    // contiguous (though it's really allocated in chunks). Given a byte-oriented view of the heap,
    // it is possible to read and write individual bytes of the heap, just like in C:
    //
    // const X = wasm.heap8u(); // a uint8-oriented view of the heap
    // X[someAddress] = 0x2a;
    // console.log( X[someAddress] ); // ==> 42
    //
    //O bviously, writing arbitrary addresses can corrupt the WASM heap, just like in C, so one has
    // to be careful with the memory addresses the work with (just like in C!).
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Fetches a single value from memory. The heap view used for reading the memory is specified
     * by the second argument, defaulting to byte-oriented view.
     *
     * If the 2nd argument ends with "*" then the pointer-sized representation is always used
     * (currently always 32 bits).
     *
     * Example:
     *
     * ```javascript
     * let i32 = wasm.peek(myPtr, 'i32');
     * ```
     */
    public fun peek(
        address: WasmPointer,
        representation: JsString
    ): JsAny

    /**
     * The second form fetches the value from each pointer in the given array and returns the array
     * of values. The heap view used for reading the memory is specified by the second argument,
     * defaulting to byte-oriented view.
     *
     * If the 2nd argument ends with "*" then the pointer-sized representation is always used
     * (currently always 32 bits).
     */
    public fun peek(
        addresses: ReadonlyArray<WasmPointer>,
        representation: JsString
    ): ReadonlyArray<JsAny>

    /**
     * Equivalent to peek(X,'*'). Most frequently used for fetching output pointer values.
     */
    public fun peekPtr(address: WasmPointer): WasmPointer

    /**
     * Equivalent to peek(X,'*'). Most frequently used for fetching output pointer values.
     */
    public fun peekPtr(addresses: ReadonlyArray<WasmPointer>): ReadonlyArray<WasmPointer>

    /**
     * Equivalent to peek(X,'i8').
     */
    public fun peek8(address: WasmPointer): JsNumber

    /**
     * Equivalent to peek(X,'i8').
     */
    public fun peek8(addresses: ReadonlyArray<WasmPointer>): ReadonlyArray<JsNumber>

    /**
     * Equivalent to peek(X,'i16').
     */
    public fun peek16(address: WasmPointer): JsNumber

    /**
     * Equivalent to peek(X,'i16').
     */
    public fun peek16(addresses: ReadonlyArray<WasmPointer>): ReadonlyArray<JsNumber>

    /**
     * Equivalent to peek(X,'i32').
     */
    public fun peek32(address: WasmPointer): JsNumber

    /**
     * Equivalent to peek(X,'i32').
     */
    public fun peek32(addresses: ReadonlyArray<WasmPointer>): ReadonlyArray<JsNumber>

    /**
     * Equivalent to peek(X,'i64').
     */
    public fun peek64(address: WasmPointer): JsBigInt

    /**
     * Equivalent to peek(X,'i64').
     */
    public fun peek64(addresses: ReadonlyArray<WasmPointer>): ReadonlyArray<JsBigInt>

    /**
     * Equivalent to peek(X,'f32').
     */
    public fun peek32f(address: WasmPointer): JsNumber

    /**
     * Equivalent to peek(X,'f32').
     */
    public fun peek32f(addresses: ReadonlyArray<WasmPointer>): ReadonlyArray<JsNumber>

    /**
     * Equivalent to peek(X,'f64').
     */
    public fun peek64f(address: WasmPointer): JsNumber

    /**
     * Equivalent to peek(X,'f64').
     */
    public fun peek64f(addresses: ReadonlyArray<WasmPointer>): ReadonlyArray<JsNumber>

    /**
     * Requires n to be one of:
     *
     * - integer 8, 16, or 32.
     * - A integer-type TypedArray constructor: Int8Array, Int16Array, Int32Array, or their Uint
     * counterparts.
     *
     * If BigInt support is enabled, it also accepts the value 64 or a BigInt64Array/BigUint64Array,
     * else it throws if passed 64 or one of those constructors.
     *
     * Returns an integer-based TypedArray view of the WASM heap memory buffer associated with the
     * given block size. If passed an integer as the first argument and unsigned is truthy then the
     * "U" (unsigned) variant of that view is returned, else the signed variant is returned. If
     * passed a TypedArray value, the 2nd argument is ignored. Note that Float32Array and
     * Float64Array views are not supported by this function.
     *
     * Be aware that growth of the heap may invalidate any references to this heap, so do not hold
     * a reference longer than needed and do not use a reference after any operation which may
     * allocate. Instead, re-fetch the reference by calling this function again, which automatically
     * refreshes the view if needed.
     *
     * Throws if passed an invalid n.
     */
    public fun heapForSize(
        n: Int,
        unsigned: Boolean
    ): TypedArray<*, *, *, *>

    /**
     * Equivalent of heapForSize(8, false) -> Int8Array.
     */
    public fun heap8(): Int8Array<*>

    /**
     * Equivalent of heapForSize(8, true) -> Uint8Array.
     */
    public fun heap8u(): Uint8Array<*>

    /**
     * Equivalent of heapForSize(16, false) -> Int16Array.
     */
    public fun heap16(): Int16Array<*>

    /**
     * Equivalent of heapForSize(16, true) -> Uint16Array.
     */
    public fun heap16u(): Uint16Array<*>

    /**
     * Equivalent of heapForSize(32, false) -> Int32Array.
     */
    public fun heap32(): Int32Array<*>

    /**
     * Equivalent of heapForSize(32, true) -> Uint32Array.
     */
    public fun heap32u(): Uint32Array<*>

    /**
     * Fetches the heapForSize() for the given representation then writes the given numeric value
     * to it. Only numbers may be written this way, and passing a non-number might trigger an
     * exception. If passed an array of pointers, it writes the given value to all of them.
     *
     * Returns this.
     */
    @IgnorableReturnValue
    public fun poke(
        address: WasmPointer,
        value: JsAny,
        representation: JsString
    ): WasmMemory

    /**
     * Fetches the heapForSize() for the given representation then writes the given numeric value
     * to it. Only numbers may be written this way, and passing a non-number might trigger an
     * exception. If passed an array of pointers, it writes the given value to all of them.
     *
     * Returns this.
     */
    @IgnorableReturnValue
    public fun poke(
        addresses: ReadonlyArray<WasmPointer>,
        value: JsAny,
        representation: JsString
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'*'). Most frequently used for fetching output pointer values.
     */
    @IgnorableReturnValue
    public fun pokePtr(
        address: WasmPointer,
        value: WasmPointer = definedExternally
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'*'). Most frequently used for fetching output pointer values.
     */
    @IgnorableReturnValue
    public fun pokePtr(
        addresses: ReadonlyArray<WasmPointer>,
        value: WasmPointer = definedExternally
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i8').
     */
    @IgnorableReturnValue
    public fun poke8(
        address: WasmPointer,
        value: Byte
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i8').
     */
    @IgnorableReturnValue
    public fun poke8(
        addresses: ReadonlyArray<WasmPointer>,
        value: Byte,
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i16').
     */
    @IgnorableReturnValue
    public fun poke16(
        address: WasmPointer,
        value: Short
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i16').
     */
    @IgnorableReturnValue
    public fun poke16(
        addresses: ReadonlyArray<WasmPointer>,
        value: Short
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i32').
     */
    @IgnorableReturnValue
    public fun poke32(
        address: WasmPointer,
        value: Int
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i32').
     */
    @IgnorableReturnValue
    public fun poke32(
        addresses: ReadonlyArray<WasmPointer>,
        value: Int
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i64').
     */
    @IgnorableReturnValue
    public fun poke64(
        address: WasmPointer,
        value: JsBigInt
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'i64').
     */
    @IgnorableReturnValue
    public fun poke64(
        addresses: ReadonlyArray<WasmPointer>,
        value: JsBigInt
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'f32').
     */
    @IgnorableReturnValue
    public fun poke32f(
        address: WasmPointer,
        value: Float
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'f32').
     */
    @IgnorableReturnValue
    public fun poke32f(
        addresses: ReadonlyArray<WasmPointer>,
        value: Float
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'f64').
     */
    @IgnorableReturnValue
    public fun poke64f(
        address: WasmPointer,
        value: Double
    ): WasmMemory

    /**
     * Equivalent to poke(X, Y,'f64').
     */
    @IgnorableReturnValue
    public fun poke64f(
        addresses: ReadonlyArray<WasmPointer>,
        value: Double
    ): WasmMemory

    ///////////////////////////////////////////////////////////////////////////
    // String Conversion and Utilities
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Expects its argument to be a pointer into the WASM heap memory which refers to a
     * NUL-terminated C-style string encoded as UTF-8.
     *
     * This function counts its byte length using cstrlen() then returns a JS-format string
     * representing its contents. As a special case, if the argument is falsy, `null` is returned.
     */
    public fun cstrToJs(ptr: WasmPointer): JsString?

    /**
     * Expects its argument to be a pointer into the WASM heap memory which refers to a
     * NUL-terminated C-style string encoded as UTF-8.
     *
     * Returns the length, in bytes, of the string, as for strlen(3). As a special case, if the
     * argument is falsy then it returns `null`.
     *
     * Throws if the argument is out of range for wasm.heap8u().
     */
    public fun cstrlen(ptr: WasmPointer): Int

    /**
     * Works similarly to C's strncpy(3), copying, at most, n bytes (not characters) from srcPtr to
     * tgtPtr. It copies until n bytes have been copied or a 0 byte is reached in src. Unlike
     * strncpy(), it returns the number of bytes it assigns in tgtPtr, including the NUL byte
     * (if any). If n is reached before a NUL byte in srcPtr, tgtPtr will not be NUL-terminated.
     * If a NUL byte is reached before n bytes are copied, tgtPtr will be NUL-terminated.
     *
     * If n is negative, cstrlen(srcPtr)+1 is used to calculate it, the +1 being for the NUL byte.
     *
     * Throws if tgtPtr or srcPtr are falsy. Results are undefined if:
     *
     * - Either is not a pointer into the WASM heap or
     * - srcPtr is not NUL-terminated AND n is less than srcPtr's logical length.
     *
     * ACHTUNG: when passing in a non-negative n value, it is possible to copy partial multibyte
     * characters this way, and converting such strings back to JS strings will have undefined
     * results.
     */
    public fun cstrncpy(
        tgtPtr: WasmPointer,
        srcPtr: WasmPointer,
        n: Int
    ): Int

    /**
     * Encodes the given JS string as UTF-8 into the given TypedArray tgt (which must be a Int8Array
     * or Uint8Array), starting at the given offset and writing, at most, maxBytes bytes (including
     * the NUL terminator if addNul is true, else no NUL is added). If it writes any bytes at all
     * and addNul is true, it always NUL-terminates the output, even if doing so means that the NUL
     * byte is all that it writes.
     *
     * If maxBytes is negative (the default) then it is treated as the remaining length of tgt,
     * starting at the given offset.
     *
     * If writing the last character would surpass the maxBytes count because the character is
     * multi-byte, that character will not be written (as opposed to writing a truncated multi-byte
     * character). This can lead to it writing as many as 3 fewer bytes than maxBytes specifies.
     *
     * Returns the number of bytes written to the target, including the NUL terminator (if any). If
     * it returns 0, it wrote nothing at all, which can happen if:
     *
     * - jsString is empty and addNul is false.
     * - offset < 0.
     * - maxBytes === 0.
     * - maxBytes is less than the byte length of a multi-byte jsString[0].
     *
     * Throws if tgt is not an Int8Array or Uint8Array.
     */
    public fun jstrcpy(
        jsString: JsString,
        tgt: Uint8Array<*>,
        offset: Int = definedExternally,
        maxBytes: Int = definedExternally,
        addNul: Boolean = definedExternally
    ): Int

    /**
     * Given a JS string, this function returns its UTF-8 length in bytes. Returns null if its
     * argument is not a string. This is a relatively expensive calculation and should be avoided
     * when not necessary.
     */
    public fun jstrlen(jsString: JsString): Int

    /**
     * For the given JS string, returns a Uint8Array of its contents encoded as UTF-8. If addNul is
     * true, the returned array will have a trailing 0 entry, else it will not.
     */
    public fun jstrToUintArray(
        jsString: JsString,
        addNul: Boolean = definedExternally
    ): Uint8Array<*>

    ///////////////////////////////////////////////////////////////////////////
    // Misc. Allocation Routines
    ///////////////////////////////////////////////////////////////////////////

    /**
     * wasm.alloc()'s srcTypedArray.byteLength bytes, populates them with the values from the source
     * TypedArray, and returns the pointer to that memory. The returned pointer must eventually be 
     * passed to wasm.dealloc() to clean it up.
     * 
     * The argument may be a Uint8Array, Int8Array, or ArrayBuffer, and it throws if passed any 
     * other type.
     * 
     * As a special case, to avoid further special cases where this routine is used, if 
     * srcTypedArray.byteLength is 0, it allocates a single byte and sets it to the value 0. Even in
     * such cases, calls must behave as if the allocated memory has exactly srcTypedArray.byteLength
     * usable bytes.
     */
    public fun allocFromTypedArray(srcTypedArray: Int8Array<*>): WasmPointer

    ///////////////////////////////////////////////////////////////////////////
    // Unofficial
    //
    // Visible and useful functions not officially documented so not part of public API
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns either aTypedArray.slice(begin,end) (if aTypedArray.buffer is a SharedArrayBuffer) or
     * aTypedArray.subarray(begin,end) (if it's not).
     */
    public fun <Array: TypedArray<*, *, *, *>> typedArrayPart(
        aTypedArray: Array,
        begin: Int,
        end: Int
    ): Array

    /**
     * Returns either aTypedArray.slice(begin,end) (if aTypedArray.buffer is a SharedArrayBuffer) or
     * aTypedArray.subarray(begin,end) (if it's not).
     */
    public fun <Array: TypedArray<*, *, *, *>> typedArrayPart(
        aTypedArray: Array,
        begin: JsBigInt,
        end: JsBigInt
    ): Array

    /**
     * Uses TextDecoder to decode the given half-open range of the given TypedArray to a string.
     */
    public fun typedArrayToString(
        typedArray: TypedArray<*, *, *, *>,
        begin: Int,
        end: Int
    ): JsString

    /**
     * Uses TextDecoder to decode the given half-open range of the given TypedArray to a string.
     */
    public fun typedArrayToString(
        typedArray: TypedArray<*, *, *, *>,
        begin: JsBigInt,
        end: JsBigInt
    ): JsString
}

///////////////////////////////////////////////////////////////////////////
// Type-safety
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [CString] from an array returned by [allocCString] and
 * [scopedAllocCStringStruct].
 */
private fun JsAny.toCString(): CString = unsafeCast<ReadonlyArray<JsAny>>().run {
    CString(
        pointer = get(0)!!.unsafeCast<WasmPointer>(),
        byteLength = get(1)!!.unsafeCast<JsBigInt>()
    )
}

/**
 * Allocates a C-style string and returns the pointer to it.
 */
public fun WasmMemory.allocCString(string: String): WasmPointer =
    allocCString(string.toJsString(), false).unsafeCast<WasmPointer>()

/**
 * Allocates a C-style string and returns a [CString] object.
 */
public fun WasmMemory.allocCStringSstruct(string: String): CString =
    allocCString(string.toJsString(), true).toCString()

/**
 * Allocates a pointer and set is to 0.
 */
public fun WasmMemory.allocPtr(): WasmPointer = allocPtr(1, true).unsafeCast<WasmPointer>()

/**
 * Allocates [howMany] pointers as a single chunk of memory and zeroes them out.
 */
public fun WasmMemory.allocPtr(howMany: UInt): ReadonlyArray<WasmPointer> = when (howMany) {
    0u -> emptyJsArray()
    1u -> jsArrayOf(allocPtr())
    else -> allocPtr(howMany.toInt(), true).unsafeCast<ReadonlyArray<WasmPointer>>()
}

/**
 * Return the size of [ir] value.
 */
public fun WasmMemory.sizeofIR(ir: IR): Int = sizeofIR(ir.value.toJsString())

/**
 * Allocates a C-style string and returns the pointer to it.
 * Must be called in a scoped allocation scope.
 */
public fun WasmMemory.scopedAllocCString(string: String): WasmPointer =
    scopedAllocCString(string.toJsString(), false).unsafeCast<WasmPointer>()

/**
 * Allocates a C-style string and returns a [CString] object.
 * Must be called in a scoped allocation scope.
 */
public fun WasmMemory.scopedAllocCStringStruct(string: String): CString =
    scopedAllocCString(string.toJsString(), true).toCString()

/**
 * Allocates a pointer and set is to 0.
 * Must be called in a scoped allocation scope.
 */
public fun WasmMemory.scopedAllocPtr(): WasmPointer = allocPtr(1, true).unsafeCast<WasmPointer>()

/**
 * Allocates [howMany] pointers as a single chunk of memory and zeroes them out.
 * Must be called in a scoped allocation scope.
 */
public fun WasmMemory.scopedAllocPtr(howMany: UInt): ReadonlyArray<WasmPointer> = when (howMany) {
    0u -> emptyJsArray()
    1u -> jsArrayOf(scopedAllocPtr())
    else -> scopedAllocPtr(howMany.toInt(), true).unsafeCast<ReadonlyArray<WasmPointer>>()
}

/**
 * Type-safe [WasmMemory.peek].
 */
public fun WasmMemory.peek(
    address: WasmPointer,
    representation: IR
): JsAny = peek(address, representation.value.toJsString())

/**
 * Type-safe [WasmMemory.peek].
 */
public fun WasmMemory.peek(
    addresses: ReadonlyArray<WasmPointer>,
    representation: IR
): ReadonlyArray<JsAny> = peek(addresses, representation.value.toJsString())

/**
 * Type-safe [WasmMemory.heapForSize].
 */
public fun WasmMemory.heapForSize(
    n: IR.Integer,
    unsigned: Boolean
): TypedArray<*, *, *, *> {
    val nInt = when (n) {
        IR.I8 -> 8
        IR.I16 -> 16
        IR.I32 -> 32
        IR.I64 -> 64
    }

    return heapForSize(nInt, unsigned)
}

/**
 * Type-safe [WasmMemory.heapForSize].
 */
public fun WasmMemory.heapForSize(
    n: IR.Number,
    unsigned: Boolean
): TypedArray<*, *, *, JsNumber> {
    val nInt = when (n) {
        IR.I8 -> 8
        IR.I16 -> 16
        IR.I32 -> 32
    }

    return heapForSize(nInt, unsigned).unsafeCast<TypedArray<*, *, *, JsNumber>>()
}

/**
 * Fetches the heapForSize() for the given representation then writes the given numeric value
 * to it. Only numbers may be written this way, and passing a non-number might trigger an
 * exception. If passed an array of pointers, it writes the given value to all of them.
 *
 * Returns this.
 */
@IgnorableReturnValue
public fun WasmMemory.poke(
    address: WasmPointer,
    value: JsNumber,
    representation: IR.Number
): WasmMemory = poke(
    address = address,
    value = value,
    representation = representation.value.toJsString()
)

/**
 * Fetches the heapForSize() for the given representation then writes the given numeric value
 * to it. Only numbers may be written this way, and passing a non-number might trigger an
 * exception. If passed an array of pointers, it writes the given value to all of them.
 *
 * Returns this.
 */
@IgnorableReturnValue
public fun WasmMemory.poke(
    addresses: ReadonlyArray<WasmPointer>,
    value: JsNumber,
    representation: IR.Number
): WasmMemory = poke(
    addresses = addresses,
    value = value,
    representation = representation.value.toJsString()
)

/**
 * Fetches the heapForSize() for the given representation then writes the given numeric value
 * to it. Only numbers may be written this way, and passing a non-number might trigger an
 * exception. If passed an array of pointers, it writes the given value to all of them.
 *
 * Returns this.
 */
@IgnorableReturnValue
public fun WasmMemory.poke(
    address: WasmPointer,
    value: JsBigInt
): WasmMemory = poke(
    address = address,
    value = value,
    representation = IR.I64.value.toJsString()
)

/**
 * Fetches the heapForSize() for the given representation then writes the given numeric value
 * to it. Only numbers may be written this way, and passing a non-number might trigger an
 * exception. If passed an array of pointers, it writes the given value to all of them.
 *
 * Returns this.
 */
@IgnorableReturnValue
public fun WasmMemory.poke(
    addresses: ReadonlyArray<WasmPointer>,
    value: JsBigInt
): WasmMemory = poke(
    addresses = addresses,
    value = value,
    representation = IR.I64.value.toJsString()
)