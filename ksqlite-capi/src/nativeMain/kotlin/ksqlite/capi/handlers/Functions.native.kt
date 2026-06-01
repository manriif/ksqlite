package ksqlite.capi.handlers

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.types.s3_context
import ksqlite.capi.types.s3_value
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3_user_data

/**
 * Handler for function callback.
 */
private inline fun functionHandler(
    context: CPointer<s3_context>?,
    call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
) {
    handler(sqlite3_user_data(context)) { function: ApplicationDefinedFunction<*>, _ ->
        function.call(sqlite3_context(context!!))
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [functionFinalHandler].
 */
internal val FunctionFinalHandler = staticCFunction(::functionFinalHandler)

/**
 * Static C function for [functionValueHandler].
 */
internal val FunctionValueHandler = staticCFunction(::functionValueHandler)

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
private fun functionFinalHandler(context: CPointer<s3_context>?) =
    functionHandler(context, ApplicationDefinedFunction<*>::callFinal)

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
private fun functionValueHandler(context: CPointer<s3_context>?) =
    functionHandler(context, ApplicationDefinedFunction<*>::callValue)

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [functionFuncHandler].
 */
internal val FunctionFuncHandler = staticCFunction(::functionFuncHandler)

/**
 * Static C function for [functionStepHandler].
 */
internal val FunctionStepHandler = staticCFunction(::functionStepHandler)

/**
 * Static C function for [functionInverseHandler].
 */
internal val FunctionInverseHandler = staticCFunction(::functionInverseHandler)

/**
 * Handler for 3 args function callback.
 */
private fun functionHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?,
    call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
) = functionHandler(context) { context ->
    call(context, argv.toArrayOrEmpty(argc) { sqlite3_value(it!!) })
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
private fun functionFuncHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callFunc)

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
private fun functionStepHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callStep)

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
private fun functionInverseHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callInverse)