package ksqlite.capi.handlers

import ksqlite.CommitHookCallback
import ksqlite.RollbackHookCallback
import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler<AppData> :
    Handler<Sqlite3CommitHookCallback<AppData>, AppData>(),
    CommitHookCallback {

    override fun call(): Int = handler { callback, appData ->
        callback.handle(appData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler<AppData> :
    Handler<Sqlite3RollbackHookCallback<AppData>, AppData>(),
    RollbackHookCallback {

    override fun call() = handler { callback, appData ->
        callback.handle(appData)
    }
}