package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.foreign.callbacks.AutovacuumPagesCallback

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutovacuumPagesHandler<AppData> :
    Handler<SqliteAutovacuumPagesCallback<AppData>, AppData>(),
    AutovacuumPagesCallback {

    override fun apply(
        zSchema: String,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = handle { callback, appData ->
        callback.apply(
            appData = appData,
            schemaName = zSchema,
            dbPage = nDbPage.toUInt(),
            freePage = nFreePage.toUInt(),
            bytePerPage = nBytePerPage.toUInt()
        ).toInt()
    }
}