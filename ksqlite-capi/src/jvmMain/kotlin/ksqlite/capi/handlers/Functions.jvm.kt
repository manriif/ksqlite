package ksqlite.capi.handlers

import ksqlite.capi.CreateFunction
import ksqlite.capi.callbacks.Sqlite3CreateFunction1Callback
import ksqlite.capi.callbacks.Sqlite3CreateFunction3Callback
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toArray
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3.sqlite3_user_data
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import kotlin.reflect.KProperty1

/**
 * Base for create function [Handler]s.
 */
internal abstract class CreateFunctionHandler<ClientData>(manager: MemoryManager) :
    Handler<ClientData>(manager) {

    /**
     * Handler for create function callback.
     */
    protected inline fun functionHandler(
        context: MemorySegment,
        block: (
            callbacks: CreateFunction<ClientData>,
            clientData: ClientData,
            context: sqlite3_context
        ) -> Unit
    ) {
        val refPointer = sqlite3_user_data(context)
        val context = sqlite3_context(context)

        handler(refPointer) { callbacks: CreateFunction<ClientData>, clientData ->
            block(callbacks, clientData, context)
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 1-arg create function [Handler]s.
 */
internal abstract class CreateFunction1ArgHandler<ClientData>(manager: MemoryManager) :
    CreateFunctionHandler<ClientData>(manager) {

    final override fun createFunctionDescriptor(): FunctionDescriptor =
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)

    /**
     * Handler for 1 arg create function callback.
     */
    protected fun functionHandler(
        context: MemorySegment,
        selector: KProperty1<CreateFunction<ClientData>, Sqlite3CreateFunction1Callback<ClientData>?>
    ) = functionHandler(context) { callbacks, clientData, context ->
        selector(callbacks)!!.handle(clientData, context)
    }
}

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionFinalHandler<ClientData>(manager: MemoryManager) :
    CreateFunction1ArgHandler<ClientData>(manager) {

    fun handle(context: MemorySegment) = functionHandler(context, CreateFunction<ClientData>::final)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionValueHandler<ClientData>(manager: MemoryManager) :
    CreateFunction1ArgHandler<ClientData>(manager) {

    fun handle(context: MemorySegment) = functionHandler(context, CreateFunction<ClientData>::value)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args create function [Handler]s.
 */
internal abstract class CreateFunction3ArgsHandler<ClientData>(manager: MemoryManager) :
    CreateFunctionHandler<ClientData>(manager) {

    final override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    /**
     * Handler for 3-args create function callback.
     */
    protected fun functionHandler(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment,
        selector: KProperty1<CreateFunction<ClientData>, Sqlite3CreateFunction3Callback<ClientData>?>
    ) = functionHandler(context) { callbacks, clientData, context ->
        val values = argv.orNull?.toArray(argc) { sqlite3_value(it) } ?: emptyArray()
        selector(callbacks)!!.handle(clientData, context, values)
    }
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class CreateFunctionFuncHandler<ClientData>(manager: MemoryManager) :
    CreateFunction3ArgsHandler<ClientData>(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, CreateFunction<ClientData>::func)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionStepHandler<ClientData>(manager: MemoryManager) :
    CreateFunction3ArgsHandler<ClientData>(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, CreateFunction<ClientData>::step)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionInverseHandler<ClientData>(manager: MemoryManager) :
    CreateFunction3ArgsHandler<ClientData>(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, CreateFunction<ClientData>::inverse)
}