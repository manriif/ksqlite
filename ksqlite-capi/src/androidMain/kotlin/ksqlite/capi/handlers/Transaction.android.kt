package ksqlite.capi.handlers

import ksqlite.foreign.callbacks.CommitHookCallback
import ksqlite.foreign.callbacks.RollbackHookCallback
import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler<AppData> :
    Handler<Sqlite3CommitHookCallback<AppData>, AppData>(),
    CommitHookCallback {

    override fun apply(): Int = handle { callback, appData ->
        callback.apply(appData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler<AppData> :
    Handler<Sqlite3RollbackHookCallback<AppData>, AppData>(),
    RollbackHookCallback {

    override fun apply() = handle { callback, appData ->
        callback.apply(appData)
    }
}