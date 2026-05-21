package ksqlite.capi.handlers

import ksqlite.AutoVacuumPagesCallback
import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutoVacuumPagesHandler(holder: Holder<Sqlite3AutoVacuumPagesCallback>) :
    Handler<Sqlite3AutoVacuumPagesCallback>(holder),
    AutoVacuumPagesCallback {

    override fun call(
        zSchema: String,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = handler { callback, userData ->
        callback(
            userData,
            zSchema,
            nDbPage.toUInt(),
            nFreePage.toUInt(),
            nBytePerPage.toUInt()
        ).toInt()
    }
}