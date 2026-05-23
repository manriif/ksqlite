package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutoVacuumPagesHandler<AppData>(manager: MemoryManager) : Handler<AppData>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT,
    )

    fun handle(
        refPointer: MemorySegment,
        zSchema: MemorySegment,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = handler(refPointer) { callback: Sqlite3AutoVacuumPagesCallback<AppData>, appData ->
        callback.handle(
            appData = appData,
            schemaName = zSchema.toKStringFromUtf8(),
            dbPage = nDbPage.toUInt(),
            freePage = nFreePage.toUInt(),
            bytePerPage = nBytePerPage.toUInt()
        ).toInt()
    }
}