package ksqlite.capi.callbacks

import ksqlite.types.SqliteResultCode

///////////////////////////////////////////////////////////////////////////
// Auto extension
///////////////////////////////////////////////////////////////////////////

internal data object AutoExtensionSuccessResult: SqliteAutoExtensionCallback.Result

internal data class AutoExtensionFailureResult(
    val result: SqliteResultCode.Failure,
    val message: String
): SqliteAutoExtensionCallback.Result

/**
 * Implementation of [SqliteAutoExtensionCallback.Scope].
 */
internal object AutoExtensionCallbackScope: SqliteAutoExtensionCallback.Scope {

    override fun success(): SqliteAutoExtensionCallback.Result {
        return AutoExtensionSuccessResult
    }

    override fun failure(
        result: SqliteResultCode.Failure,
        message: String
    ): SqliteAutoExtensionCallback.Result {
        return AutoExtensionFailureResult(result, message)
    }
}