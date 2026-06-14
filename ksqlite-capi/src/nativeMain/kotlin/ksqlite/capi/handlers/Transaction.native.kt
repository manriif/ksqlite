package ksqlite.capi.handlers

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback

///////////////////////////////////////////////////////////////////////////
// Commit
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [commitHookHandler].
 */
internal val CommitHookHandler = staticCFunction(::commitHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
private fun commitHookHandler(
    refPointer: COpaquePointer?
) = handle(refPointer) { callback: SqliteCommitHookCallback<Any?>, appData ->
    callback.apply(appData)
}

///////////////////////////////////////////////////////////////////////////
// Rollback
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [rollbackHookHandler].
 */
internal val RollbackHookHandler = staticCFunction(::rollbackHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
private fun rollbackHookHandler(
    refPointer: COpaquePointer?
) = handle(refPointer) { callback: SqliteRollbackHookCallback<Any?>, appData ->
    callback.apply(appData)
}