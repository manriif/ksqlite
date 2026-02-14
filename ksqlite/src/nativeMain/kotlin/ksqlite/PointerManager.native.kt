package ksqlite

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pin
import kotlin.reflect.KFunction0

/**
 * Provides [CPointer] to objects, managing their lifecycles.
 */
public open class PointerManager internal constructor() {

    private lateinit var arena: Arena
    private lateinit var disposables: MutableList<KFunction0<Unit>>

    /**
     * Adds a dispose function that will be invoked once on [clear].
     */
    private fun addDisposable(dispose: KFunction0<Unit>) {
        if (::disposables.isInitialized) {
            disposables.add(dispose)
        } else {
            disposables = mutableListOf(dispose)
        }
    }

    /**
     * Returns a [CPointer] to [value]'s reference.
     */
    internal fun pointer(value: Any): COpaquePointer {
        val stableRef = StableRef.create(value)
        addDisposable(stableRef::dispose)
        return stableRef.asCPointer()
    }

    /**
     * Returns a [CPointer] to [value]'s content.
     */
    internal fun pointer(value: ByteArray): CPointer<ByteVar> {
        val pinned = value.pin()
        addDisposable(pinned::unpin)
        return pinned.addressOf(0)
    }

    /**
     * Allocates a copy of the [value] and returns a [CPointer] to the content.
     */
    internal fun pointer(value: String): CPointer<ByteVar> {
        if (!::arena.isInitialized) {
            arena = Arena()
        }

        return value.cstr.getPointer(arena)
    }


    /**
     * Clears all the allocated memory and unpins all the pinneds objects.
     */
    public fun clear() {
        if (::arena.isInitialized) {
            arena.clear()
        }

        if (::disposables.isInitialized) {
            disposables.onEach(KFunction0<Unit>::invoke).clear()
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates a copy of the [value] and returns a [CPointer] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun PointerManager.pointer(value: Any?): COpaquePointer? = value?.let(::pointer)

/**
 * Pins [value] and returns a [CPointer] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun PointerManager.pointer(value: ByteArray?): CPointer<ByteVar>? = value?.let(::pointer)

/**
 * Allocates a copy of the [value] and returns a [CPointer] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun PointerManager.pointer(value: String?): CPointer<ByteVar>? = value?.let(::pointer)
