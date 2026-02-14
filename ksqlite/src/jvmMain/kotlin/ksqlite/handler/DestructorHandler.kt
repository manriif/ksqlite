package ksqlite.handler

import ksqlite.MemoryManager
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler that only release reference to object to make it available for GC.
 */
internal class DestructorHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor =
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)

    fun handle(userPtr: MemorySegment) {
        manager.clear(userPtr)
    }
}