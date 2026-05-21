package ksqlite.capi.handlers

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.CreateFunction
import ksqlite.capi.callbacks.Sqlite3CreateFunction1Callback
import ksqlite.capi.callbacks.Sqlite3CreateFunction3Callback
import ksqlite.capi.types.s3_context
import ksqlite.capi.types.s3_value
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.memory.toArray
import ksqlite.sqlite3_user_data
import kotlin.reflect.KProperty1

/**
 * Handler for create function callback.
 */
private inline fun functionHandler(
    context: CPointer<s3_context>?,
    block: (
        callbacks: CreateFunction,
        userData: sqlite3_mutable_pointer?,
        context: sqlite3_context
    ) -> Unit
) {
    val refPointer = sqlite3_user_data(context)
    val context = sqlite3_context(context!!)

    handler(refPointer) { callbacks: CreateFunction, userData ->
        block(callbacks, userData, context)
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
 * Handler for 1 arg create function callback.
 */
private fun functionHandler1Arg(
    context: CPointer<s3_context>?,
    selector: KProperty1<CreateFunction, Sqlite3CreateFunction1Callback?>
) = functionHandler(context) { callbacks, userData, context ->
    selector(callbacks)!!.invoke(userData, context)
}

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionFinalHandler(context: CPointer<s3_context>?) =
    functionHandler1Arg(context, CreateFunction::final)

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionValueHandler(context: CPointer<s3_context>?) =
    functionHandler1Arg(context, CreateFunction::value)

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
private fun functionHandler3Args(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?,
    selector: KProperty1<CreateFunction, Sqlite3CreateFunction3Callback?>
) = functionHandler(context) { callbacks, userData, context ->
    val values = argv?.toArray(argc) { sqlite3_value(it!!) } ?: emptyArray()
    selector(callbacks)!!.invoke(userData, context, values)
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
private fun createFunctionFuncHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler3Args(context, argc, argv, CreateFunction::func)

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionStepHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler3Args(context, argc, argv, CreateFunction::step)

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
private fun createFunctionInverseHandler(
    context: CPointer<s3_context>?,
    argc: Int,
    argv: CPointer<CPointerVar<s3_value>>?
) = functionHandler3Args(context, argc, argv, CreateFunction::inverse)