package ksqlite.capi.memory

import org.sqlite.jni.capi.NativePointerHolder

public actual open class GenericPointer internal constructor(internal val pointer: Long)

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns [W] instantiated after [factory] which is passed the non-null pointing instance [H]
 *
 */
internal inline fun <H: NativePointerHolder<*>, W> H?.wrapOrNull(factory: (H) -> W): W? {
    if (this == null || nativePointer == 0L) {
        return null
    }

    return factory(this)
}