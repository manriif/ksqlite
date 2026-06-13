package ksqlite.capi.handlers

import ksqlite.callbacks.AutovacuumPagesCallback
import ksqlite.capi.callbacks.Sqlite3AutovacuumPagesCallback

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutovacuumPagesHandler<AppData> :
    Handler<Sqlite3AutovacuumPagesCallback<AppData>, AppData>(),
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