package ksqlite.memory

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Holder for pointer.
 */
public abstract class Pointer internal constructor() : PointerBase<MemorySegment, MemorySegment>() {

    override fun doAllocation(
        allocate: (MemorySegment) -> Int,
        handlePointer: (MemorySegment?) -> Unit
    ): Int = Arena.ofConfined().use { arena ->
        val varPointer = arena.allocate(ValueLayout.ADDRESS)

        allocate(varPointer).also {
            handlePointer(varPointer.get(ValueLayout.ADDRESS, 0))
        }
    }
}