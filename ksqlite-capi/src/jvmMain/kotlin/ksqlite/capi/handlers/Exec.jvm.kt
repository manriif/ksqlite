package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3ExecCallback
import ksqlite.capi.memory.getStringUtf8
import ksqlite.capi.memory.getStringUtf8OrNull
import ksqlite.capi.memory.toArray
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
internal class ExecHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
    )

    fun handle(
        refPointer: MemorySegment,
        columnCount: Int,
        values: MemorySegment,
        names: MemorySegment
    ): Int = handler(refPointer) { callback: Sqlite3ExecCallback, userData ->
        val columnValues = values.toArray(columnCount) { it.getStringUtf8OrNull() }
        val columnNames = names.toArray(columnCount) { it.getStringUtf8() }

        callback(
            userData,
            columnCount,
            columnValues,
            columnNames
        )
    }
}