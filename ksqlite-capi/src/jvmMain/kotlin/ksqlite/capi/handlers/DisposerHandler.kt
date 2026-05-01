package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler that dispose reference to object to make it available for GC.
 */
internal class DisposerHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor =
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)

    fun handle(userPtr: MemorySegment) {
        manager.getStrongReference(userPtr).dispose()
    }
}