package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3AutovacuumPagesCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.`sqlite3_autovacuum_pages$x0`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutovacuumPagesHandler :
    Handler(),
    `sqlite3_autovacuum_pages$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_autovacuum_pages$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        zSchema: MemorySegment,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = handle(refPointer) { callback: Sqlite3AutovacuumPagesCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            schemaName = zSchema.toKStringFromUtf8(),
            dbPage = nDbPage.toUInt(),
            freePage = nFreePage.toUInt(),
            bytePerPage = nBytePerPage.toUInt()
        ).toInt()
    }
}