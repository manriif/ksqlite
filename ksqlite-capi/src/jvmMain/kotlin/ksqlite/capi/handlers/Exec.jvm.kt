package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.memory.toArray
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
internal class ExecHandler<ClientData>(manager: MemoryManager) :
    Handler<ClientData>(manager) {

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
    ): Int = handler(refPointer) { callback: Sqlite3ExecCallback<ClientData>, data ->
        val columnValues = values.toArray(columnCount) { it.toKStringFromUtf8OrNull() }
        val columnNames = names.toArray(columnCount) { it.toKStringFromUtf8() }

        callback.handle(
            clientData = data,
            columnCount = columnCount,
            columnValues = columnValues,
            columnNames = columnNames
        )
    }
}