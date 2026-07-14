/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.capi.vtab.callbacks

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.types.SqliteResultCode
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode
import ksqlite.capi.vtab.callbacks.SqliteVtabCreateOrConnectCallback as CreateOrConnect
import ksqlite.capi.vtab.callbacks.SqliteVtabFindFunctionCallback as FindFunction
import ksqlite.capi.vtab.callbacks.SqliteVtabIntegrityCallback as Integrity
import ksqlite.capi.vtab.callbacks.SqliteVtabOpenCallback as Open
import ksqlite.capi.vtab.callbacks.SqliteVtabRowidCallback as Rowid
import ksqlite.capi.vtab.callbacks.SqliteVtabUpdateCallback as Update

/**
 * Common result for scope emitting a [result] on failure.
 */
internal class VtabResultFailureResult(val result: SqliteResultCode.Failure) :
    Open.Result<Nothing>,
    Rowid.Result,
    Update.Result

///////////////////////////////////////////////////////////////////////////
// Create or connect
///////////////////////////////////////////////////////////////////////////

internal class VtabCreateOrConnectSuccessResult<Vtab : sqlite3_vtab>(val vTab: Vtab) :
    CreateOrConnect.Result<Vtab>

internal class VtabCreateOrConnectFailureResult(val error: String) :
    CreateOrConnect.Result<Nothing>

/**
 * Implementation of [CreateOrConnect.Scope].
 *
 * Singleton with erased type to reduce unnecessary allocation and also the concrete type is not
 * manipulated internally.
 */
private object VtabCreateOrConnectCallbackScope : CreateOrConnect.Scope<sqlite3_vtab> {

    override fun success(vTab: sqlite3_vtab): CreateOrConnect.Result<sqlite3_vtab> =
        VtabCreateOrConnectSuccessResult(vTab)

    override fun failure(error: String): CreateOrConnect.Result<sqlite3_vtab> =
        VtabCreateOrConnectFailureResult(error)
}

/**
 * Returns [VtabCreateOrConnectCallbackScope].
 */
@Suppress("UNCHECKED_CAST")
internal fun <Vtab : sqlite3_vtab> vTabCreateOrConnectScope(): CreateOrConnect.Scope<Vtab> =
    VtabCreateOrConnectCallbackScope as CreateOrConnect.Scope<Vtab>

///////////////////////////////////////////////////////////////////////////
// Open
///////////////////////////////////////////////////////////////////////////

internal class VtabOpenSuccessResult<VtabCursor : sqlite3_vtab_cursor>(val cursor: VtabCursor) :
    Open.Result<VtabCursor>

/**
 * Implementation of [Open.Scope].
 *
 * Singleton with erased type to reduce unnecessary allocation and also the concrete type is not
 * manipulated internally.
 */
private object VtabOpenCallbackScope : Open.Scope<sqlite3_vtab_cursor> {

    override fun success(cursor: sqlite3_vtab_cursor): Open.Result<sqlite3_vtab_cursor> =
        VtabOpenSuccessResult(cursor)

    override fun failure(result: SqliteResultCode.Failure): Open.Result<sqlite3_vtab_cursor> =
        VtabResultFailureResult(result)
}

/**
 * Returns [VtabOpenCallbackScope].
 */
@Suppress("UNCHECKED_CAST")
internal fun <VtabCursor : sqlite3_vtab_cursor> vTabOpenScope(): Open.Scope<VtabCursor> =
    VtabOpenCallbackScope as Open.Scope<VtabCursor>

///////////////////////////////////////////////////////////////////////////
// Rowid
///////////////////////////////////////////////////////////////////////////

internal class VtabRowidSuccessResult(val rowid: Long) : Rowid.Result

/**
 * Implementation of [Rowid.Scope].
 */
private object VtabRowidCallbackScope : Rowid.Scope {

    override fun success(rowid: Long): Rowid.Result =
        VtabRowidSuccessResult(rowid)

    override fun failure(result: SqliteResultCode.Failure): Rowid.Result =
        VtabResultFailureResult(result)
}

/**
 * Returns [VtabRowidCallbackScope].
 */
internal fun vTabRowidScope(): Rowid.Scope = VtabRowidCallbackScope

///////////////////////////////////////////////////////////////////////////
// Update
///////////////////////////////////////////////////////////////////////////

internal class VtabUpdateSuccessResult(val rowid: Long?) : Update.Result

/**
 * Implementation of [Update.Scope].
 */
private object VtabUpdateCallbackScope : Update.Scope {

    override fun success(rowid: Long?): Update.Result =
        VtabUpdateSuccessResult(rowid)

    override fun failure(result: SqliteResultCode.Failure): Update.Result =
        VtabResultFailureResult(result)
}

/**
 * Returns [VtabUpdateCallbackScope].
 */
internal fun vTabUpdateScope(): Update.Scope = VtabUpdateCallbackScope

///////////////////////////////////////////////////////////////////////////
// FindFunction
///////////////////////////////////////////////////////////////////////////

internal class VtabFindFunctionOverloadResult(
    val result: Int,
    val appData: Any?,
    val function: SqliteFunctionFuncCallback<Any?>,
    val destroy: SqliteDestroyCallback<Any?>?
) : FindFunction.Result

internal object VtabFindFunctionDoNotOverloadResult : FindFunction.Result

/**
 * Implementation of [FindFunction.Scope].
 */
private object VtabFindFunctionCallbackScope : FindFunction.Scope {

    @Suppress("UNCHECKED_CAST")
    private fun overload(
        result: Int,
        appData: Any?,
        function: SqliteFunctionFuncCallback<*>,
        destroy: SqliteDestroyCallback<*>? = null
    ) = VtabFindFunctionOverloadResult(
        result = result,
        appData = appData,
        function = function as SqliteFunctionFuncCallback<Any?>,
        destroy = destroy as? SqliteDestroyCallback<Any?>
    )

    override fun overload(function: SqliteFunctionFuncCallback<Nothing?>): FindFunction.Result =
        overload(1, null, function)

    override fun <AppData> overload(
        appData: AppData,
        function: SqliteFunctionFuncCallback<in AppData>,
        destroy: SqliteDestroyCallback<in AppData>?
    ): FindFunction.Result = overload(1, appData, function, destroy)

    override fun overload(
        constraintOp: SqliteVtabConstraintOperatorCode.Custom,
        function: SqliteFunctionFuncCallback<Nothing?>
    ): FindFunction.Result = overload(constraintOp.code, null, function)

    override fun <AppData> overload(
        constraintOp: SqliteVtabConstraintOperatorCode.Custom,
        appData: AppData,
        function: SqliteFunctionFuncCallback<in AppData>,
        destroy: SqliteDestroyCallback<in AppData>?
    ): FindFunction.Result = overload(constraintOp.code, appData, function, destroy)

    override fun doNotOverload(): FindFunction.Result = VtabFindFunctionDoNotOverloadResult
}

/**
 * Returns [VtabFindFunctionCallbackScope].
 */
internal fun vTabFindFunctionScope(): FindFunction.Scope = VtabFindFunctionCallbackScope

///////////////////////////////////////////////////////////////////////////
// Integrity
///////////////////////////////////////////////////////////////////////////

internal class VtabIntegritySuccessResult(val error: String?) : Integrity.Result

/**
 * Failure result for [Integrity].
 */
internal class VtabIntegrityFailureResult(
    val error: String,
    val result: SqliteResultCode.Failure
) : Integrity.Result

/**
 * Implementation of [Integrity.Scope].
 */
private object VtabIntegrityCallbackScope : Integrity.Scope {

    override fun success(error: String?): Integrity.Result =
        VtabIntegritySuccessResult(error)

    override fun failure(
        error: String,
        result: SqliteResultCode.Failure
    ): Integrity.Result = VtabIntegrityFailureResult(error, result)
}

/**
 * Returns [VtabIntegrityCallbackScope].
 */
internal fun vTabIntegrityScope(): Integrity.Scope = VtabIntegrityCallbackScope