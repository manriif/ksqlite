package ksqlite.capi.handlers

import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.exports
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

/**
 * Base for function [Handler]s.
 */
internal abstract class FunctionHandler : Handler() {

    /**
     * Handler for function callback.
     */
    protected inline fun handleFunction(
        context: WasmPointer,
        call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
    ) {
        handle(exports.sqlite3_user_data(context)) { function: ApplicationDefinedFunction<*>, _ ->
            function.call(sqlite3_context(context))
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 1-arg function [Handler]s.
 */
internal abstract class Function1ArgHandler : FunctionHandler() {

    final override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = this::apply
    )

    /**
     * Handles the function call.
     */
    protected abstract fun apply(context: WasmPointer)
}

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionFinalHandler : Function1ArgHandler() {

    override fun apply(context: WasmPointer) =
        handleFunction(context, ApplicationDefinedFunction<*>::callFinal)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionValueHandler : Function1ArgHandler() {

    override fun apply(context: WasmPointer) =
        handleFunction(context, ApplicationDefinedFunction<*>::callValue)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args function [Handler]s.
 */
internal abstract class Function3ArgsHandler : FunctionHandler() {

    final override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer
        ),
        function = this::apply
    )

    /**
     * Handler for 3-args function callback.
     */
    protected fun handleFunction(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer,
        call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
    ) = handleFunction(context) { context ->
        call(context, argv.toArrayOrEmpty(argc) { sqlite3_value(it) })
    }

    /**
     * Handles the function call.
     */
    protected abstract fun apply(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    )
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class FunctionFuncHandler : Function3ArgsHandler() {

    override fun apply(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callFunc)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionStepHandler : Function3ArgsHandler() {

    override fun apply(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callStep)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionInverseHandler : Function3ArgsHandler() {

    override fun apply(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callInverse)
}