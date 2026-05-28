package ksqlite.capi.handlers

import ksqlite.ProgressHandlerCallback
import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
internal class ProgressHandlerHandler<AppData> :
    Handler<Sqlite3ProgressHandlerCallback<AppData>, AppData>(),
    ProgressHandlerCallback {

    override fun call() = handler { callback, appData ->
        callback.handle(appData)
    }
}