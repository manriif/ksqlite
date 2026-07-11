package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteProgressHandlerCallback
import ksqlite.foreign.callbacks.ProgressHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
internal class ProgressHandlerHandler<AppData> :
    Handler<SqliteProgressHandlerCallback<AppData>, AppData>(),
    ProgressHandlerCallback {

    override fun apply() = handle { callback, appData ->
        callback.apply(appData)
    }
}