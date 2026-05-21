package ksqlite.capi.handlers

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback

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
) = handler(refPointer) { callback: Sqlite3CommitHookCallback, userData ->
    callback(userData)
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
) = handler(refPointer) { callback: Sqlite3RollbackHookCallback, userData ->
    callback(userData)
}