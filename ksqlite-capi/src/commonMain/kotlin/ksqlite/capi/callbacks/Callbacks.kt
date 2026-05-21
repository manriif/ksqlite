package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3Result

///////////////////////////////////////////////////////////////////////////
// Auto extension
///////////////////////////////////////////////////////////////////////////

/**
 * Success result for [Sqlite3AutoExtensionCallback].
 */
internal data object AutoExtensionSuccessResult: Sqlite3AutoExtensionCallback.Result

/**
 * FFailure result for [Sqlite3AutoExtensionCallback].
 */
internal data class AutoExtensionFailureResult(
    val result: Sqlite3Result.Failure,
    val message: String
): Sqlite3AutoExtensionCallback.Result

/**
 * Implementation of [Sqlite3AutoExtensionCallback.Scope].
 */
internal object AutoExtensionCallbackScope: Sqlite3AutoExtensionCallback.Scope {

    override fun success(): Sqlite3AutoExtensionCallback.Result {
        return AutoExtensionSuccessResult
    }

    override fun failure(
        result: Sqlite3Result.Failure,
        message: String
    ): Sqlite3AutoExtensionCallback.Result {
        return AutoExtensionFailureResult(result, message)
    }
}