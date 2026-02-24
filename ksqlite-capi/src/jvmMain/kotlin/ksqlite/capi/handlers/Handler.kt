package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import java.lang.foreign.FunctionDescriptor

/**
 * Handler for native callback.
 *
 * Must have a method `handle` with a signature matching the [FunctionDescriptor] returned by
 * [createFunctionDescriptor].
 */
internal abstract class Handler(protected val manager: MemoryManager) {

    abstract fun createFunctionDescriptor(): FunctionDescriptor
}