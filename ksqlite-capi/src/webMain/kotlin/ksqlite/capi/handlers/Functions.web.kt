package ksqlite.capi.handlers

import ksqlite.capi.CreateFunction
import ksqlite.capi.exports
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toArray
import ksqlite.capi.callbacks.Sqlite3CreateFunction1Callback
import ksqlite.capi.callbacks.Sqlite3CreateFunction3Callback
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_value
import kotlin.reflect.KProperty1

/**
 * Base for create function [Handler]s.
 */
internal abstract class CreateFunctionHandler(manager: MemoryManager) : Handler(manager) {

    /**
     * Handler for create function callback.
     */
    protected inline fun functionHandler(
        context: WasmPointer,
        block: (
            callbacks: CreateFunction,
            userData: sqlite3_mutable_pointer?,
            context: sqlite3_context
        ) -> Unit
    ) {
        val refPointer = exports.sqlite3_user_data(context)
        val context = sqlite3_context(context)

        handler(refPointer) { callbacks: CreateFunction, userData ->
            block(callbacks, userData, context)
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 1-arg create function [Handler]s.
 */
internal abstract class CreateFunction1ArgHandler(
    manager: MemoryManager,
    private val selector: KProperty1<CreateFunction, Sqlite3CreateFunction1Callback?>
) : CreateFunctionHandler(manager) {

    final override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = ::handle
    )

    /**
     * Handler for 1 arg create function callback.
     */
    private fun handle(
        context: WasmPointer,
    ) = functionHandler(context) { callbacks, userData, context ->
        selector(callbacks)!!.invoke(userData, context)
    }
}

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionFinalHandler(manager: MemoryManager) :
    CreateFunction1ArgHandler(manager, CreateFunction::final)

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionValueHandler(manager: MemoryManager) :
    CreateFunction1ArgHandler(manager, CreateFunction::value)

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args create function [Handler]s.
 */
internal abstract class CreateFunction3ArgsHandler(
    manager: MemoryManager,
    private val
    selector: KProperty1<CreateFunction, Sqlite3CreateFunction3Callback?>
) : CreateFunctionHandler(manager) {

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
    private fun handle(
        context: WasmPointer,
        argc: Int,
        argv: WasmPointer,
        selector: KProperty1<CreateFunction, Sqlite3CreateFunction3Callback?>
    ) = functionHandler(context) { callbacks, userData, context ->
        val values = argv.orNull?.toArray(argc) { sqlite3_value(it) } ?: emptyArray()
        selector(callbacks)!!.invoke(userData, context, values)
    }
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class CreateFunctionFuncHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager, CreateFunction::func)

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionStepHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager, CreateFunction::step)

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionInverseHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager, CreateFunction::inverse)