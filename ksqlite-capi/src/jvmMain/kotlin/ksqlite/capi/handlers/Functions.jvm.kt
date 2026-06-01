package ksqlite.capi.handlers

import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toArray
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3.sqlite3_user_data
import ksqlite.`sqlite3_create_function_v2$xFinal`
import ksqlite.`sqlite3_create_function_v2$xFunc`
import ksqlite.`sqlite3_create_function_v2$xStep`
import ksqlite.`sqlite3_create_window_function$xInverse`
import ksqlite.`sqlite3_create_window_function$xValue`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Base for function [Handler]s.
 */
internal abstract class FunctionHandler : Handler() {

    /**
     * Handler for function callback.
     */
    protected inline fun handleFunction(
        context: MemorySegment,
        call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
    ) {
        handle(sqlite3_user_data(context)) { function: ApplicationDefinedFunction<*>, _ ->
            function.call(sqlite3_context(context))
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionFinalHandler :
    FunctionHandler(),
    `sqlite3_create_function_v2$xFinal`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_function_v2$xFinal`.allocate(this, arena)

    override fun apply(context: MemorySegment) =
        handleFunction(context, ApplicationDefinedFunction<*>::callFinal)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionValueHandler :
    FunctionHandler(),
    `sqlite3_create_window_function$xValue`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_window_function$xValue`.allocate(this, arena)

    override fun apply(context: MemorySegment) =
        handleFunction(context, ApplicationDefinedFunction<*>::callValue)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args function [Handler]s.
 */
internal abstract class Function3ArgsHandler : FunctionHandler() {

    /**
     * Handler for 3-args function callback.
     */
    protected fun handleFunction(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment,
        call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
    ) = handleFunction(context) { context ->
        call(context, argv.orNull?.toArray(argc) { sqlite3_value(it) } ?: emptyArray())
    }
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class FunctionFuncHandler :
    Function3ArgsHandler(),
    `sqlite3_create_function_v2$xFunc`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_function_v2$xFunc`.allocate(this, arena)

    override fun apply(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callFunc)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionStepHandler :
    Function3ArgsHandler(),
    `sqlite3_create_function_v2$xStep`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_function_v2$xStep`.allocate(this, arena)

    override fun apply(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callStep)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionInverseHandler :
    Function3ArgsHandler(),
    `sqlite3_create_window_function$xInverse`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_window_function$xInverse`.allocate(this, arena)

    override fun apply(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callInverse)
}