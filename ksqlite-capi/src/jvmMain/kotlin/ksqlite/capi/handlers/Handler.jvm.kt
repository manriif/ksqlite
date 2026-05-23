package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.stableRefData
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment

/**
 * Handler for native callback.
 *
 * Must have a method `handle` with a signature matching the [FunctionDescriptor] returned by
 * [createFunctionDescriptor].
 */
internal abstract class Handler<AppData>(protected val manager: MemoryManager) {

    /**
     * Returns the [FunctionDescriptor].
     */
    abstract fun createFunctionDescriptor(): FunctionDescriptor

    /**
     * Returns [block]'s result, invoked with [Data] and optional userData obtained from a
     * previously referenced [refPointer].
     */
    protected inline fun <reified Data : Any, Result> handler(
        refPointer: MemorySegment,
        block: (data: Data, appData: AppData) -> Result
    ): Result {
        val (data, appData) = manager.stableRefData<Data, AppData>(refPointer)
        return block(data, appData)
    }
}