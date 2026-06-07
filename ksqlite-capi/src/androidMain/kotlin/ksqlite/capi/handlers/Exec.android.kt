package ksqlite.capi.handlers

import ksqlite.callbacks.ExecCallback
import ksqlite.capi.callbacks.Sqlite3ExecCallback

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class ExecHandler<AppData> :
    Handler<Sqlite3ExecCallback<AppData>, AppData>(),
    ExecCallback {

    override fun apply(
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    ): Int = handle { callback, appData ->
        callback.apply(
            appData = appData,
            columnCount = columnCount,
            columnValues = columnValues,
            columnNames = columnNames
        )
    }
}