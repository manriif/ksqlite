package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler<ClientData>(manager: MemoryManager) :
    Handler<ClientData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT
    )

    fun handle(
        refPointer: MemorySegment,
        count: Int,
    ): Int = handler(refPointer) { callback: Sqlite3BusyHandlerCallback<ClientData>, data ->
        callback.handle(
            clientData = data,
            count = count
        )
    }
}