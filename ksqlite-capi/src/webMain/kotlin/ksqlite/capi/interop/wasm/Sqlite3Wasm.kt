@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package ksqlite.capi.interop.wasm

import kotlin.js.JsAny
import kotlin.js.JsString

/**
 * The [sqlite3.wasm](https://sqlite.org/wasm/doc/trunk/api-wasm.md) namespace, abbreviated as wasm
 * for the remainder of this page, holds a number of routines for working with WASM-side constructs.
 * They include APIs for such tasks as...
 *
 * - Memory management.
 *     - Allocating and freeing memory.
 *     - Helpers for working with WASM heap memory, e.g. getting and setting primitive values
 *     from/to the WASM heap.
 * - Configurable result value and argument type conversion for WASM-exported functions.
 * - JS/C String conversions.
 * - Binding JS functions into the WASM runtime, so that they may be called from WASM code (i.e.
 * from C).
 *
 * In short, if a WASM-specific feature has been needed during the development of the sqlite3 JS API,
 * it's been added to this namespace. For the most part, high-level client code will rarely need to
 * make use of more than a few of these, whereas clients using the C-style APIs may make heavy use
 * of them.
 */
internal external interface Sqlite3Wasm {

    val exports: Sqlite3WasmExports
    val ptr: Ptr

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
    fun alloc(): FunctionWithImpl<WasmPointer>

    /**
     * Uses alloc() to allocate enough memory for the byte-length of the given JS string, plus 1
     * (for a NUL terminator), copies the given JS string to that memory using jstrcpy(),
     * NUL-terminates it, and returns the pointer to that C-string. Ownership of the pointer is
     * transfered to the caller, who must eventually pass the pointer to dealloc() to free it.
     *
     * If passed a truthy 2nd argument then its return semantics change: it returns [ptr,n], where
     * ptr is the C-string's pointer and n is its cstrlen().
     */
    fun allocCString(jsString: JsString): WasmPointer

    /**
     * Uses alloc() to allocate enough memory for the byte-length of the given JS string, plus 1
     * (for a NUL terminator), copies the given JS string to that memory using jstrcpy(),
     * NUL-terminates it, and returns the pointer to that C-string. Ownership of the pointer is
     * transfered to the caller, who must eventually pass the pointer to dealloc() to free it.
     *
     * If passed a truthy 2nd argument then its return semantics change: it returns [ptr,n], where
     * ptr is the C-string's pointer and n is its cstrlen().
     *
     * [returnWithLength] must be set to `true`
     */
    fun allocCString(jsString: JsString, returnWithLength: Boolean): Array<JsAny>

    /**
     * Expects its argument to be a pointer into the WASM heap memory which refers to a
     * NUL-terminated C-style string encoded as UTF-8.
     *
     * This function counts its byte length using cstrlen() then returns a JS-format string
     * representing its contents. As a special case, if the argument is falsy, `null` is returned.
     */
    fun cstrToJs(ptr: WasmPointer): String

    /**
     * Expects its argument to be a pointer into the WASM heap memory which refers to a
     * NUL-terminated C-style string encoded as UTF-8.
     *
     * Returns the length, in bytes, of the string, as for strlen(3). As a special case, if the
     * argument is falsy then it returns `null`.
     *
     * Throws if the argument is out of range for wasm.heap8u().
     */
    fun cstrlen(ptr: WasmPointer): Int
}