package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.foreign.`sqlite3_wal_hook$x0`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
internal class WalHookHandler :
    Handler(),
    `sqlite3_wal_hook$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_wal_hook$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        db: MemorySegment,
        dbName: MemorySegment,
        nPage: Int,
    ): Int = handle(refPointer) { callback: SqliteWalHookCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            databaseName = dbName.toKStringFromUtf8(),
            pageCount = nPage
        ).code
    }
}