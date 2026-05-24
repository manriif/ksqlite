package ksqlite.capi.handlers

import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.exports
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toArray
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

/**
 * Base for create function [Handler]s.
 */
internal abstract class CreateFunctionHandler(manager: MemoryManager) : Handler(manager) {

    /**
     * Handler for create function callback.
     */
    protected inline fun functionHandler(
        context: WasmPointer,
        call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
    ) {
        handler(exports.sqlite3_user_data(context)) { function: ApplicationDefinedFunction<*>, _ ->
            function.call(sqlite3_context(context))
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 1-arg create function [Handler]s.
 */
internal abstract class CreateFunction1ArgHandler(manager: MemoryManager) :
    CreateFunctionHandler(manager) {

    final override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = ::handle
    )

    /**
     * Handles the function call.
     */
    protected abstract fun handle(context: WasmPointer)
}

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionFinalHandler(manager: MemoryManager) :
    CreateFunction1ArgHandler(manager) {

    override fun handle(context: WasmPointer) =
        functionHandler(context, ApplicationDefinedFunction<*>::callFinal)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionValueHandler(manager: MemoryManager) :
    CreateFunction1ArgHandler(manager) {

    override fun handle(context: WasmPointer) =
        functionHandler(context, ApplicationDefinedFunction<*>::callValue)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args create function [Handler]s.
 */
internal abstract class CreateFunction3ArgsHandler(manager: MemoryManager) :
    CreateFunctionHandler(manager) {

    final override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer
        ),
        function = ::handle
    )

    /**
     * Handler for 3-args create function callback.
     */
    protected fun functionHandler(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer,
        call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
    ) = functionHandler(context) { context ->
        call(context, argv.orNull?.toArray(argc) { sqlite3_value(it) } ?: emptyArray())
    }

    /**
     * Handles the function call.
     */
    protected abstract fun handle(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    )
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class CreateFunctionFuncHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager) {

    override fun handle(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callFunc)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionStepHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager) {

    override fun handle(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callStep)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionInverseHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager) {

    override fun handle(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer
    ) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callInverse)
}