package ksqlite.capi

import ksqlite.capi.types.Sqlite3CreateFunctionFinalCallback
import ksqlite.capi.types.Sqlite3CreateFunctionFuncCallback
import ksqlite.capi.types.Sqlite3CreateFunctionInverseCallback
import ksqlite.capi.types.Sqlite3CreateFunctionStepCallback
import ksqlite.capi.types.Sqlite3CreateFunctionValueCallback

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * Holder for [sqlite3_create_function] and [sqlite3_create_function_v2] and
 * [sqlite3_create_window_function] callbacks.
 */
internal class CreateFunction private constructor(
    val func: Sqlite3CreateFunctionFuncCallback?,
    val step: Sqlite3CreateFunctionStepCallback?,
    val final: Sqlite3CreateFunctionFinalCallback?,
    val value: Sqlite3CreateFunctionValueCallback?,
    val inverse: Sqlite3CreateFunctionInverseCallback?,
) {

    /**
     * Regular function.
     */
    constructor(
        func: Sqlite3CreateFunctionFuncCallback?,
        step: Sqlite3CreateFunctionStepCallback?,
        final: Sqlite3CreateFunctionFinalCallback?
    ) : this(
        func = func,
        step = step,
        final = final,
        value = null,
        inverse = null
    )

    /**
     * Window function.
     */
    constructor(
        step: Sqlite3CreateFunctionStepCallback?,
        final: Sqlite3CreateFunctionFinalCallback?,
        value: Sqlite3CreateFunctionValueCallback?,
        inverse: Sqlite3CreateFunctionInverseCallback?,
    ) : this(
        func = null,
        step = step,
        final = final,
        value = value,
        inverse = inverse
    )
}