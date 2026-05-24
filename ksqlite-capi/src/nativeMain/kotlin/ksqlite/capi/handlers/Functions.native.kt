package ksqlite.capi.handlers

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.memory.toArray
import ksqlite.capi.types.s3_context
import ksqlite.capi.types.s3_value
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3_user_data

/**
 * Handler for create function callback.
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
 * Static C function for [createFunctionFinalHandler].
 */
internal val CreateFunctionFinalHandler = staticCFunction(::createFunctionFinalHandler)

/**
 * Static C function for [createFunctionValueHandler].
 */
internal val CreateFunctionValueHandler = staticCFunction(::createFunctionValueHandler)

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionFinalHandler(context: CPointer<s3_context>?) =
    functionHandler(context, ApplicationDefinedFunction<*>::callFinal)

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionValueHandler(context: CPointer<s3_context>?) =
    functionHandler(context, ApplicationDefinedFunction<*>::callValue)

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [createFunctionFuncHandler].
 */
internal val CreateFunctionFuncHandler = staticCFunction(::createFunctionFuncHandler)

/**
 * Static C function for [createFunctionStepHandler].
 */
internal val CreateFunctionStepHandler = staticCFunction(::createFunctionStepHandler)

/**
 * Static C function for [createFunctionInverseHandler].
 */
internal val CreateFunctionInverseHandler = staticCFunction(::createFunctionInverseHandler)

/**
 * Handler for 3 args create function callback.
 */
private fun functionHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?,
    call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
) = functionHandler(context) { context ->
    call(context, argv?.toArray(argc) { sqlite3_value(it!!) } ?: emptyArray())
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
private fun createFunctionFuncHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callFunc)

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionStepHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callStep)

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionInverseHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler(context, argc, argv, ApplicationDefinedFunction<*>::callInverse)