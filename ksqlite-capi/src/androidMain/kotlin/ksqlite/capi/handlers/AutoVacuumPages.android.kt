package ksqlite.capi.handlers

import ksqlite.AutoVacuumPagesCallback
import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutoVacuumPagesHandler<AppData> :
    Handler<Sqlite3AutoVacuumPagesCallback<AppData>, AppData>(),
    AutoVacuumPagesCallback {

    override fun call(
        zSchema: String,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = handler { callback, appData ->
        callback.handle(
            appData = appData,
            schemaName = zSchema,
            dbPage = nDbPage.toUInt(),
            freePage = nFreePage.toUInt(),
            bytePerPage = nBytePerPage.toUInt()
        ).toInt()
    }
}