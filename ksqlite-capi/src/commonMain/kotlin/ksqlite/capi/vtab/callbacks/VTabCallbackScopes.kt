package ksqlite.capi.vtab.callbacks

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.types.SqliteResultCode
import ksqlite.types.vtab.SqliteVTabConstraintOperatorCode
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.capi.vtab.callbacks.SqliteVTabCreateOrConnectCallback as CreateOrConnect
import ksqlite.capi.vtab.callbacks.SqliteVTabFindFunctionCallback as FindFunction
import ksqlite.capi.vtab.callbacks.SqliteVTabIntegrityCallback as Integrity
import ksqlite.capi.vtab.callbacks.SqliteVTabOpenCallback as Open
import ksqlite.capi.vtab.callbacks.SqliteVTabRowidCallback as Rowid
import ksqlite.capi.vtab.callbacks.SqliteVTabUpdateCallback as Update

/**
 * Common result for scope emitting a [result] on failure.
 */
internal class VTabResultFailureResult(val result: SqliteResultCode.Failure) :
    Open.Result<Nothing>,
    Rowid.Result,
    Update.Result

///////////////////////////////////////////////////////////////////////////
// Create or connect
///////////////////////////////////////////////////////////////////////////

internal class VTabCreateOrConnectSuccessResult<VTab : sqlite3_vtab>(val vTab: VTab) :
    CreateOrConnect.Result<VTab>

internal class VTabCreateOrConnectFailureResult(val error: String) :
    CreateOrConnect.Result<Nothing>

/**
 * Implementation of [CreateOrConnect.Scope].
 *
 * Singleton with erased type to reduce unnecessary allocation and also the concrete type is not
 * manipulated internally.
 */
private object VTabCreateOrConnectCallbackScope : CreateOrConnect.Scope<sqlite3_vtab> {

    override fun success(vTab: sqlite3_vtab): CreateOrConnect.Result<sqlite3_vtab> =
        VTabCreateOrConnectSuccessResult(vTab)

    override fun failure(error: String): CreateOrConnect.Result<sqlite3_vtab> =
        VTabCreateOrConnectFailureResult(error)
}

/**
 * Returns [VTabCreateOrConnectCallbackScope].
 */
@Suppress("UNCHECKED_CAST")
internal fun <VTab : sqlite3_vtab> vTabCreateOrConnectScope(): CreateOrConnect.Scope<VTab> =
    VTabCreateOrConnectCallbackScope as CreateOrConnect.Scope<VTab>

///////////////////////////////////////////////////////////////////////////
// Open
///////////////////////////////////////////////////////////////////////////

internal class VTabOpenSuccessResult<VTabCursor : sqlite3_vtab_cursor>(val cursor: VTabCursor) :
    Open.Result<VTabCursor>

/**
 * Implementation of [Open.Scope].
 *
 * Singleton with erased type to reduce unnecessary allocation and also the concrete type is not
 * manipulated internally.
 */
private object VTabOpenCallbackScope : Open.Scope<sqlite3_vtab_cursor> {

    override fun success(cursor: sqlite3_vtab_cursor): Open.Result<sqlite3_vtab_cursor> =
        VTabOpenSuccessResult(cursor)

    override fun failure(result: SqliteResultCode.Failure): Open.Result<sqlite3_vtab_cursor> =
        VTabResultFailureResult(result)
}

/**
 * Returns [VTabOpenCallbackScope].
 */
@Suppress("UNCHECKED_CAST")
internal fun <VTabCursor : sqlite3_vtab_cursor> vTabOpenScope(): Open.Scope<VTabCursor> =
    VTabOpenCallbackScope as Open.Scope<VTabCursor>

///////////////////////////////////////////////////////////////////////////
// Rowid
///////////////////////////////////////////////////////////////////////////

internal class VTabRowidSuccessResult(val rowid: Long) : Rowid.Result

/**
 * Implementation of [Rowid.Scope].
 */
private object VTabRowidCallbackScope : Rowid.Scope {

    override fun success(rowid: Long): Rowid.Result =
        VTabRowidSuccessResult(rowid)

    override fun failure(result: SqliteResultCode.Failure): Rowid.Result =
        VTabResultFailureResult(result)
}

/**
 * Returns [VTabRowidCallbackScope].
 */
internal fun vTabRowidScope(): Rowid.Scope = VTabRowidCallbackScope

///////////////////////////////////////////////////////////////////////////
// Update
///////////////////////////////////////////////////////////////////////////

internal class VTabUpdateSuccessResult(val rowid: Long?) : Update.Result

/**
 * Implementation of [Update.Scope].
 */
private object VTabUpdateCallbackScope : Update.Scope {

    override fun success(rowid: Long?): Update.Result =
        VTabUpdateSuccessResult(rowid)

    override fun failure(result: SqliteResultCode.Failure): Update.Result =
        VTabResultFailureResult(result)
}

/**
 * Returns [VTabUpdateCallbackScope].
 */
internal fun vTabUpdateScope(): Update.Scope = VTabUpdateCallbackScope

///////////////////////////////////////////////////////////////////////////
// FindFunction
///////////////////////////////////////////////////////////////////////////

internal class VTabFindFunctionOverloadResult(
    val result: Int,
    val appData: Any?,
    val function: SqliteFunctionFuncCallback<Any?>,
    val destroy: SqliteDestroyCallback<Any?>?
) : FindFunction.Result

internal object VTabFindFunctionDoNotOverloadResult : FindFunction.Result

/**
 * Implementation of [FindFunction.Scope].
 */
private object VTabFindFunctionCallbackScope : FindFunction.Scope {

    @Suppress("UNCHECKED_CAST")
    private fun overload(
        result: Int,
        appData: Any?,
        function: SqliteFunctionFuncCallback<*>,
        destroy: SqliteDestroyCallback<*>? = null
    ) = VTabFindFunctionOverloadResult(
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
        constraintOp: SqliteVTabConstraintOperatorCode.Custom,
        function: SqliteFunctionFuncCallback<Nothing?>
    ): FindFunction.Result = overload(constraintOp.code, null, function)

    override fun <AppData> overload(
        constraintOp: SqliteVTabConstraintOperatorCode.Custom,
        appData: AppData,
        function: SqliteFunctionFuncCallback<in AppData>,
        destroy: SqliteDestroyCallback<in AppData>?
    ): FindFunction.Result = overload(constraintOp.code, appData, function, destroy)

    override fun doNotOverload(): FindFunction.Result = VTabFindFunctionDoNotOverloadResult
}

/**
 * Returns [VTabFindFunctionCallbackScope].
 */
internal fun vTabFindFunctionScope(): FindFunction.Scope = VTabFindFunctionCallbackScope

///////////////////////////////////////////////////////////////////////////
// Integrity
///////////////////////////////////////////////////////////////////////////

internal class VTabIntegritySuccessResult(val error: String?) : Integrity.Result

/**
 * Failure result for [Integrity].
 */
internal class VTabIntegrityFailureResult(
    val error: String,
    val result: SqliteResultCode.Failure
) : Integrity.Result

/**
 * Implementation of [Integrity.Scope].
 */
private object VTabIntegrityCallbackScope : Integrity.Scope {

    override fun success(error: String?): Integrity.Result =
        VTabIntegritySuccessResult(error)

    override fun failure(
        error: String,
        result: SqliteResultCode.Failure
    ): Integrity.Result = VTabIntegrityFailureResult(error, result)
}

/**
 * Returns [VTabIntegrityCallbackScope].
 */
internal fun vTabIntegrityScope(): Integrity.Scope = VTabIntegrityCallbackScope