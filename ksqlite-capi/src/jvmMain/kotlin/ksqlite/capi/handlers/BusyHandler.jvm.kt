package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.foreign.`sqlite3_busy_handler$x0`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler :
    Handler(),
    `sqlite3_busy_handler$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_busy_handler$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        count: Int,
    ): Int = handle(refPointer) { callback: SqliteBusyHandlerCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            count = count
        )
    }
}