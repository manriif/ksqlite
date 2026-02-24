package ksqlite.memory

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer

/**
 * Holder for pointer to [Struct].
 */
public abstract class Pointer<Struct : CPointed> internal constructor(pointer: CPointer<Struct>) :
    PointerBase<CPointer<Struct>>(pointer)