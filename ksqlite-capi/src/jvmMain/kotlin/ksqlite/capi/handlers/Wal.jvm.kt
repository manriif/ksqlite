package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3WalHookCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.`sqlite3_wal_hook$x0`
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
    ): Int = handle(refPointer) { callback: Sqlite3WalHookCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            dbName = dbName.toKStringFromUtf8(),
            nPage = nPage
        ).code
    }
}