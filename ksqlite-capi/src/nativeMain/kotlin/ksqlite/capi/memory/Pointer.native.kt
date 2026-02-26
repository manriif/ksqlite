package ksqlite.capi.memory

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer

/**
 * Holder for pointer to [Struct].
 */
public abstract class SimplePointer<Struct : CPointed> internal constructor(
    internal val pointer: CPointer<Struct>
)

/**
 * Holder for pointer to [Struct] with an associated [MemoryManager].
 */
public abstract class ManagedPointer<Struct : CPointed> internal constructor(
    pointer: CPointer<Struct>,
    restricted: Boolean
) : MemoryPointer<CPointer<Struct>>(pointer, restricted)