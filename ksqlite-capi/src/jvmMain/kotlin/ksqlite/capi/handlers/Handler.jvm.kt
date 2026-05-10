package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.getReferencedData
import ksqlite.capi.types.sqlite3_mutable_pointer
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment

/**
 * Handler for native callback.
 *
 * Must have a method `handle` with a signature matching the [FunctionDescriptor] returned by
 * [createFunctionDescriptor].
 */
internal abstract class Handler(protected val manager: MemoryManager) {

    /**
     * Returns the [FunctionDescriptor].
     */
    abstract fun createFunctionDescriptor(): FunctionDescriptor

    /**
     * Returns [block]'s result, invoked with [Data] and optional userData obtained from a
     * previously referenced [refPointer].
     */
    protected inline fun <reified Data : Any, Result> handle(
        refPointer: MemorySegment,
        block: (data: Data, userData: sqlite3_mutable_pointer?) -> Result
    ): Result {
        val (data, userData) = manager
            .getStableRef(refPointer)
            .getReferencedData<Data>()

        return block(data, userData)
    }
}