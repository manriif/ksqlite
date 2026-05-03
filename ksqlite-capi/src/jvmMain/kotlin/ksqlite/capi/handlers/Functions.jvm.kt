package ksqlite.capi.handlers

import ksqlite.capi.CreateFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3CreateFunction1Callback
import ksqlite.capi.types.Sqlite3CreateFunction3Callback
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.utils.orNull
import ksqlite.capi.utils.toArray
import ksqlite.sqlite3.sqlite3_user_data
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import kotlin.reflect.KProperty1

/**
 * Base for create function [Handler]s.
 */
internal abstract class CreateFunctionHandler(manager: MemoryManager) : Handler(manager) {

    /**
     * Handler for create function callback.
     */
    protected inline fun functionHandler(
        context: MemorySegment,
        block: (
            callbacks: CreateFunction,
            userData: sqlite3_mutable_pointer?,
            context: sqlite3_context
        ) -> Unit
    ) {
        val refPointer = sqlite3_user_data(context)
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
internal abstract class CreateFunction1ArgHandler(manager: MemoryManager) :
    CreateFunctionHandler(manager) {

    final override fun createFunctionDescriptor(): FunctionDescriptor =
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)

    /**
     * Handler for 1 arg create function callback.
     */
    protected fun functionHandler(
        context: MemorySegment,
        selector: KProperty1<CreateFunction, Sqlite3CreateFunction1Callback?>
    ) = functionHandler(context) { callbacks, userData, context ->
        selector(callbacks)!!.invoke(userData, context)
    }
}

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionFinalHandler(manager: MemoryManager) :
    CreateFunction1ArgHandler(manager) {

    fun handle(context: MemorySegment) = functionHandler(context, CreateFunction::final)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionValueHandler(manager: MemoryManager) :
    CreateFunction1ArgHandler(manager) {

    fun handle(context: MemorySegment) = functionHandler(context, CreateFunction::value)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 1-arg create function [Handler]s.
 */
internal abstract class CreateFunction3ArgsHandler(manager: MemoryManager) :
    CreateFunctionHandler(manager) {

    final override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    /**
     * Handler for 1 arg create function callback.
     */
    protected fun functionHandler(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment,
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
    CreateFunction3ArgsHandler(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, CreateFunction::func)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionStepHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, CreateFunction::step)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class CreateFunctionInverseHandler(manager: MemoryManager) :
    CreateFunction3ArgsHandler(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, CreateFunction::inverse)
}