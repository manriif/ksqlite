package ksqlite.capi

import ksqlite.capi.types.Sqlite3CreateFunctionFinalCallback
import ksqlite.capi.types.Sqlite3CreateFunctionFuncCallback
import ksqlite.capi.types.Sqlite3CreateFunctionStepCallback

/**
 * Holder for [sqlite3_create_function] and [sqlite3_create_function_v2] callback.
 */
internal data class CreateFunctionCallbacks(
    val func: Sqlite3CreateFunctionFuncCallback?,
    val step: Sqlite3CreateFunctionStepCallback?,
    val final: Sqlite3CreateFunctionFinalCallback?,
)