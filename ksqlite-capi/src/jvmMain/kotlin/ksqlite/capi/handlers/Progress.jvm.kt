package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback
import ksqlite.`sqlite3_progress_handler$x0`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
internal class ProgressHandlerHandler :
    Handler(),
    `sqlite3_progress_handler$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_progress_handler$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment
    ): Int = handle(refPointer) { callback: Sqlite3ProgressHandlerCallback<Any?>, appData ->
        callback.apply(appData)
    }
}