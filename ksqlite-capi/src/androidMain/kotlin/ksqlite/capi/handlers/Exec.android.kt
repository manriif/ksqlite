package ksqlite.capi.handlers

import ksqlite.foreign.callbacks.ExecCallback
import ksqlite.capi.callbacks.SqliteExecCallback

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class ExecHandler<AppData> :
    Handler<SqliteExecCallback<AppData>, AppData>(),
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