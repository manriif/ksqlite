package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
internal class ProgressHandlerHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment
    ): Int = handler(refPointer) { callback: Sqlite3ProgressHandlerCallback<AppData>, appData ->
        callback.handle(appData)
    }
}