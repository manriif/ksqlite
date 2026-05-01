package ksqlite.capi.handlers

import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.memory.MemoryManager
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages] callback.
 */
internal class AutovacuumPagesHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_INT,
    )

    fun handle(
        userPtr: MemorySegment,
        zSchema: MemorySegment,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = manager
        .getStrongRefData<Sqlite3AutoVacuumPagesCallback>(userPtr)
        .invoke(
            userPtr,
            zSchema.getString(0),
            nDbPage.toUInt(),
            nFreePage.toUInt(),
            nBytePerPage.toUInt()
        )
        .toInt()
}