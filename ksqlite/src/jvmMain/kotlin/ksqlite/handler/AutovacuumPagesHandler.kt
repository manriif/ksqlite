package ksqlite.handler

import ksqlite.AutovacuumPages
import ksqlite.MemoryManager
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.sqlite3_autovacuum_pages] callback.
 */
internal class AutovacuumPagesHandler<Data>(manager: MemoryManager) : Handler(manager) {

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
    ): Int = manager.get<AutovacuumPages<Data>>(userPtr).run {
        checkNotNull(callback)
            .invoke(
                data,
                zSchema.getString(0),
                nDbPage.toUInt(),
                nFreePage.toUInt(),
                nBytePerPage.toUInt()
            )
            .toInt()
    }
}