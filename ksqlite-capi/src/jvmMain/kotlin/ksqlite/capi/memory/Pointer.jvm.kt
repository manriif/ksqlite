package ksqlite.capi.memory

import java.lang.foreign.MemorySegment

/**
 * Holder for pointer.
 */
public abstract class Pointer internal constructor(pointer: MemorySegment) :
    Pointer<MemorySegment>(pointer)