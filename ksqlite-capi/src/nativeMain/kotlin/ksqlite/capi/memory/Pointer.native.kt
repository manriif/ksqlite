package ksqlite.capi.memory

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.PointerBase

/**
 * Holder for pointer to [Struct].
 */
public abstract class Pointer<Struct : CPointed> internal constructor(pointer: CPointer<Struct>) :
    PointerBase<CPointer<Struct>>(pointer)