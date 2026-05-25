package ksqlite.capi.handlers

import ksqlite.FunctionCallback
import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

/**
 * Base for function [Handler]s.
 */
internal abstract class FunctionHandler : Handler<ApplicationDefinedFunction<*>, Nothing?>() {

    /**
     * Handler for function callback.
     */
    protected inline fun functionHandler(
        context: Long,
        call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
    ) {
        handler { function, _ ->
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
internal abstract class Function1ArgHandler : FunctionHandler(), FunctionCallback.Func1

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionFinalHandler : Function1ArgHandler(), FunctionCallback.Final {

    override fun call(context: Long) =
        functionHandler(context, ApplicationDefinedFunction<*>::callFinal)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionValueHandler : Function1ArgHandler(), FunctionCallback.Value {

    override fun call(context: Long) =
        functionHandler(context, ApplicationDefinedFunction<*>::callValue)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args function [Handler]s.
 */
internal abstract class Function3ArgsHandler : FunctionHandler(), FunctionCallback.Func2 {

    /**
     * Handler for 3-args function callback.
     */
    protected fun functionHandler(
        context: Long,
        values: LongArray,
        call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
    ) = functionHandler(context) { context ->
        call(context, values.map(::sqlite3_value).toTypedArray())
    }
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class FunctionFuncHandler : Function3ArgsHandler(), FunctionCallback.Func {

    override fun call(
        context: Long,
        values: LongArray
    ) = functionHandler(context, values, ApplicationDefinedFunction<*>::callFunc)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionStepHandler : Function3ArgsHandler(), FunctionCallback.Step {

    override fun call(
        context: Long,
        values: LongArray
    ) = functionHandler(context, values, ApplicationDefinedFunction<*>::callStep)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionInverseHandler : Function3ArgsHandler(), FunctionCallback.Inverse {

    override fun call(
        context: Long,
        values: LongArray
    ) = functionHandler(context, values, ApplicationDefinedFunction<*>::callInverse)
}