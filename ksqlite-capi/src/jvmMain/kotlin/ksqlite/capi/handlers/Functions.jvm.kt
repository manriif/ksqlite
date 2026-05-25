package ksqlite.capi.handlers

import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toArray
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3.sqlite3_user_data
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Base for function [Handler]s.
 */
internal abstract class FunctionHandler(manager: MemoryManager) : Handler(manager) {

    /**
     * Handler for function callback.
     */
    protected inline fun functionHandler(
        context: MemorySegment,
        call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
    ) {
        handler(sqlite3_user_data(context)) { function: ApplicationDefinedFunction<*>, _ ->
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
internal abstract class Function1ArgHandler(manager: MemoryManager) :
    FunctionHandler(manager) {

    final override fun createFunctionDescriptor(): FunctionDescriptor =
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
}

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionFinalHandler(manager: MemoryManager) :
    Function1ArgHandler(manager) {

    fun handle(context: MemorySegment) =
        functionHandler(context, ApplicationDefinedFunction<*>::callFinal)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionValueHandler(manager: MemoryManager) :
    Function1ArgHandler(manager) {

    fun handle(context: MemorySegment) =
        functionHandler(context, ApplicationDefinedFunction<*>::callValue)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args function [Handler]s.
 */
internal abstract class Function3ArgsHandler(manager: MemoryManager) :
    FunctionHandler(manager) {

    final override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    /**
     * Handler for 3-args function callback.
     */
    protected fun functionHandler(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment,
        call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
    ) = functionHandler(context) {context ->
        call(context, argv.orNull?.toArray(argc) { sqlite3_value(it) } ?: emptyArray())
    }
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class FunctionFuncHandler(manager: MemoryManager) :
    Function3ArgsHandler(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callFunc)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionStepHandler(manager: MemoryManager) :
    Function3ArgsHandler(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callStep)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionInverseHandler(manager: MemoryManager) :
    Function3ArgsHandler(manager) {

    fun handle(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callInverse)
}