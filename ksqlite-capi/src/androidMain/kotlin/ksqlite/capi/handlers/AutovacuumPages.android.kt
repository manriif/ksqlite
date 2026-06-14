package ksqlite.capi.handlers

import ksqlite.foreign.callbacks.AutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback

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