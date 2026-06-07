package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.memory.toNullableStringArrayOrEmpty
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.`sqlite3_exec$callback`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
internal class ExecHandler :
    Handler(),
    `sqlite3_exec$callback`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_exec$callback`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        columnCount: Int,
        values: MemorySegment,
        names: MemorySegment
    ): Int = handle(refPointer) { callback: Sqlite3ExecCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            columnCount = columnCount,
            columnValues = values.toNullableStringArrayOrEmpty(columnCount),
            columnNames = names.toStringArrayOrEmpty(columnCount)
        )
    }
}