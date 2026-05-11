@file:Suppress("DEPRECATION")

package ksqlite.capi.interop.wasm

import ksqlite.capi.interop.js.arrayJoinToString
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsString
import kotlin.js.nativeInvoke
import kotlin.js.toJsString

internal typealias WasmFunction = JsAny

/**
 * A WASM module exposes all exported functions to the user, but they are in "raw" form. That is,
 * they perform no argument or result type conversion and only support data types supported by WASM
 * (i.e. only numeric types). That's fine for functions which only accept and return numbers, but is
 * generally less helpful for functions which take or return strings or have output pointers. For
 * usability reasons, it's desirable to reduce the JS/C friction by automatically performing mundane
 * tasks such as the allocation and deallocation of memory needed for converting strings between JS
 * and WASM.
 *
 * Additionally, it's often useful to add new functions to the WASM runtime from JS, which requires
 * compiling binary WASM code on the fly. A common example of this is creating user-defined SQL
 * functions. For the most part, the JS bindings of the sqlite3 API take care of such conversions
 * for the user, but there are cases where client code will need to, or want to, perform such
 * conversions itself.
 *
 * [Sqlite3 Functions](https://sqlite.org/wasm/doc/trunk/api-wasm.md)
 */
internal external interface WasmFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // WASM Function Table
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Given a function pointer, returns the WASM function table entry if found, else returns a
     * falsy value.
     */
    fun functionEntry(ptr: WasmPointer): JsAny?

    /**
     * Returns the WASM module's indirect function table.
     */
    fun functionTable(): JsAny

    ///////////////////////////////////////////////////////////////////////////
    // Calling and Wrapping Functions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Calls a WASM-exported function by name, passing on all supplied arguments (which may
     * optionally be supplied as an array). If throws if the function is not exported or if the
     * argument count does not match. This routine does no type conversion and is essentially
     * equivalent to:
     *
     * ```javascript
     * const rc = wasm.exports.some_func(...args)
     * ```
     *
     * with the exception that xCall() throws if the argument count does not match that of the
     * WASM-exported function.
     */
    fun xCall(
        functionName: JsString,
        vararg args: JsAny
    ): JsAny

    /**
     * Calls a WASM-exported function by name, passing on all supplied arguments (which may
     * optionally be supplied as an array). If throws if the function is not exported or if the
     * argument count does not match. This routine does no type conversion and is essentially
     * equivalent to:
     *
     * ```javascript
     * const rc = wasm.exports.some_func(...args)
     * ```
     *
     * with the exception that xCall() throws if the argument count does not match that of the
     * WASM-exported function.
     */
    fun xCall(
        functionName: JsString,
        args: JsArray<JsAny>
    ): JsAny

    /**
     * Functions like xCall() but performs argument and result type conversions as for xWrap().
     *
     * The first argument is the name of the exported function to call. The 2nd its the name of its
     * result type, as documented for xWrap(). The 3rd is an array of argument type names, as
     * documented for xWrap(). The 4th+ arguments are arguments for the call, with the special case
     * that if the 4th argument is an array, it is used as the arguments for the call.
     *
     * Returns the converted result of the call.
     *
     * This is just a thin wrapper around xWrap(). If the given function is to be called more than
     * once, it's more efficient to use xWrap() to create a wrapper, then to call that wrapper as
     * many times as needed. For one-shot calls, however, this variant is arguably more efficient
     * because it will hypothetically free the wrapper function quickly.
     */
    fun xCallWrapped(
        functionName: JsString,
        resultType: JsString,
        argsTypes: JsArray<JsString>,
        vararg args: JsAny
    ): JsAny

    /**
     * Functions like xCall() but performs argument and result type conversions as for xWrap().
     *
     * The first argument is the name of the exported function to call. The 2nd its the name of its
     * result type, as documented for xWrap(). The 3rd is an array of argument type names, as
     * documented for xWrap(). The 4th+ arguments are arguments for the call, with the special case
     * that if the 4th argument is an array, it is used as the arguments for the call.
     *
     * Returns the converted result of the call.
     *
     * This is just a thin wrapper around xWrap(). If the given function is to be called more than
     * once, it's more efficient to use xWrap() to create a wrapper, then to call that wrapper as
     * many times as needed. For one-shot calls, however, this variant is arguably more efficient
     * because it will hypothetically free the wrapper function quickly.
     */
    fun xCallWrapped(
        functionName: JsString,
        resultType: JsString,
        argsTypes: JsArray<JsString>,
        args: JsArray<JsAny>
    ): JsAny

    /**
     * Returns a WASM-exported function by name, or throws if the function is not found.
     */
    fun xGet(functionName: JsString): WasmFunction

    /**
     * xWrap() creates a JS function which calls a WASM-exported function, as described for xCall().
     *
     * Creates a wrapper for the WASM-exported function fname. It uses xGet() to fetch the exported
     * function (which throws on error) and returns either that function or a wrapper for that
     * function which converts the JS-side argument types into WASM-side types and converts the
     * result type. If the function takes no arguments and resultType is null then the function is
     * returned as-is, else a wrapper is created for it to adapt its arguments and result value.
     */
    fun xWrap(
        functionName: JsString,
        resultType: JsString,
        vararg argsTypes: JsString
    )

    /**
     * xWrap() creates a JS function which calls a WASM-exported function, as described for xCall().
     *
     * Creates a wrapper for the WASM-exported function fname. It uses xGet() to fetch the exported
     * function (which throws on error) and returns either that function or a wrapper for that
     * function which converts the JS-side argument types into WASM-side types and converts the
     * result type. If the function takes no arguments and resultType is null then the function is
     * returned as-is, else a wrapper is created for it to adapt its arguments and result value.
     */
    fun xWrap(
        functionName: JsString,
        resultType: JsString,
        argsTypes: JsArray<JsString>
    )

    ///////////////////////////////////////////////////////////////////////////
    // (Un)Installing WASM Functions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Expects a JS function and signature, exactly as for wasm.jsFuncToWasm(). It uses that
     * function to create a WASM-exported function, installs that function to the next available
     * slot of wasm.functionTable(), and returns the function's index in that table (which acts as
     * a pointer to that function). The returned pointer can be passed to wasm.uninstallFunction()
     * to uninstall it and free up the table slot for reuse.
     *
     * As a special case, if the passed-in function is a WASM-exported function then the signature
     * argument is ignored and func is installed as-is, without requiring re-compilation/re-wrapping.
     *
     * This function will propagate an exception if WebAssembly.Table.grow() throws or
     * wasm.jsFuncToWasm() throws. The former case can happen in an Emscripten-compiled environment
     * when building without Emscripten's -sALLOW_TABLE_GROWTH flag.
     */
    fun installFunction(
        funcSignature: JsString,
        function: JsFunction
    ): WasmPointer

    /**
     * Creates a WASM function which wraps the given JS function and returns the JS binding of that
     * WASM function. The function signature string must be in the form used by jaccwabyt or
     * Emscripten's addFunction(). In short: in may have one of the following formats:
     *
     * - Emscripten: "x...", where the first x is a letter representing the result type and subsequent
     * letters represent the argument types. See below. Functions with no arguments have only a
     * single letter.
     * - Jaccwabyt: "x(...)" where x is the letter representing the result type and letters in the
     * parens (if any) represent the argument types. Functions with no arguments use x(). See below.
     *
     * Supported letters:
     *
     * - i = int32
     * - p = int32 ("pointer")
     * - j = int64
     * - f = float32
     * - d = float64
     * - v = void, only legal for use as the result type
     *
     * It throws if an invalid signature letter is used.
     */
    fun jsFuncToWasm(
        signature: JsString,
        function: JsFunction,
    ): WasmFunction

    /**
     * This works exactly like installFunction() except that the installation is scoped to the
     * current allocation scope and is uninstalled when the current allocation scope is popped.
     * It will throw if no allocation scope is active.
     */
    fun scopedInstallFunction(
        funcSignature: JsString,
        function: JsFunction
    ): WasmPointer

    /**
     * Requires a pointer value previously returned from wasm.installFunction(). Removes that
     * function from the WASM function table, marks its table slot as free for re-use, and returns
     * that function. It is illegal to call this before installFunction() has been called and
     * results are undefined if the argument was not returned by that function. The returned
     * function may be passed back to installFunction() to reinstall it.
     */
    fun uninstallFunction(pointer: WasmPointer): WasmFunction
}

///////////////////////////////////////////////////////////////////////////
// Type-safety
///////////////////////////////////////////////////////////////////////////

/**
 * Type-safe function signature types in Emscripten format.
 */
internal value class FunctionSignature(val signature: String) {

    sealed interface Parameter {

        val value: Char
    }

    sealed interface Result {

        val value: Char

        operator fun invoke(vararg parameterTypes: Parameter): FunctionSignature {
            val parameters = arrayJoinToString(parameterTypes, "", Parameter::value)
            return FunctionSignature("$value$parameters")
        }
    }

    data object Pointer : Result, Parameter {
        override val value: Char
            get() = 'p'
    }

    data object Int32 : Result, Parameter {
        override val value: Char
            get() = 'i'
    }

    data object Int64 : Result, Parameter {
        override val value: Char
            get() = 'j'
    }

    data object Float32 : Result, Parameter {
        override val value: Char
            get() = 'f'
    }

    data object Float64 : Result, Parameter {
        override val value: Char
            get() = 'd'
    }

    data object Void : Result {
        override val value: Char
            get() = 'v'
    }
}

/**
 * JS invokable function.
 */
internal external interface JsFunction : JsAny {

    @nativeInvoke
    operator fun invoke(vararg args: JsAny): JsAny
}

/**
 * Installs a JS function.
 */
internal fun WasmFunctions.installFunction(
    signature: FunctionSignature,
    function: Function<*>
): WasmPointer {
    return installFunction(
        funcSignature = signature.signature.toJsString(),
        function = function as JsFunction
    )
}