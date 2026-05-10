package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3BusyHandlerCallback
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT
    )

    fun handle(
        refPointer: MemorySegment,
        count: Int,
    ): Int = handle(refPointer) { callback: Sqlite3BusyHandlerCallback, userData ->
        callback(
            userData,
            count
        )
    }
}