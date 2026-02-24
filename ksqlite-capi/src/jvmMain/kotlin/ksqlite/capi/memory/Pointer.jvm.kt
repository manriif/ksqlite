package ksqlite.capi.memory

import ksqlite.capi.memory.PointerBase
import java.lang.foreign.MemorySegment

/**
 * Holder for pointer.
 */
public abstract class Pointer internal constructor(pointer: MemorySegment) :
    PointerBase<MemorySegment>(pointer)