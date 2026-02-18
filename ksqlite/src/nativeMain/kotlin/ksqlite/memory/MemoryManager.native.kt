package ksqlite.memory

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pin

/**
 * Manages memory.
 */
public open class MemoryManager internal constructor() : AutoCloseable {

    private lateinit var pointers: MutableSet<ManagedPointerImpl>
    private lateinit var pinneds: MutableList<Pinned<*>>
    private lateinit var arena: Arena
    private var closed = false

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Implementation of [ManagedPointer].
     */
    private inner class ManagedPointerImpl(private val value: Any) : ManagedPointer {

        val stableRef = StableRef.create(this)

        @Suppress("UNCHECKED_CAST")
        override fun <Data : Any> get(): Data {
            return value as Data
        }

        override fun dispose() {
            check(pointers.remove(this)) {
                "Pointer is not managed"
            }

            stableRef.dispose()
        }
    }

    /**
     * Returns a stable [COpaquePointer] to [value].
     */
    internal fun managedPointer(value: Any): COpaquePointer = notClosed {
        val managed = ManagedPointerImpl(value)

        if (::pointers.isInitialized) {
            pointers.add(managed)
        } else {
            pointers = mutableSetOf(managed)
        }

        managed.stableRef.asCPointer()
    }

    /**
     * Returns a [CPointer] to [value]'s content.
     */
    internal fun bufferPointer(value: ByteArray): CPointer<ByteVar> = notClosed {
        val pinned = value.pin()

        if (::pinneds.isInitialized) {
            pinneds.add(pinned)
        } else {
            pinneds = mutableListOf(pinned)
        }

        pinned.addressOf(0)
    }

    /**
     * Allocates a copy of the [value] and returns a [CPointer] to the content.
     */
    internal fun stringPointer(value: String): CPointer<ByteVar> = notClosed {
        if (!::arena.isInitialized) {
            arena = Arena()
        }

        value.cstr.getPointer(arena)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cleanup
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Clears all the allocated memory and releases all the pinned/referenced objects.
     */
    internal fun clear() = notClosed {
        if (::pointers.isInitialized) {
            pointers.onEach(ManagedPointerImpl::dispose).clear()
        }

        if (::pinneds.isInitialized) {
            pinneds.onEach(Pinned<*>::unpin).clear()
        }

        if (::arena.isInitialized) {
            arena.clear()
        }
    }

    /**
     * Invokes and returns [block]'s result throwing an [IllegalStateException] if this instance is
     * closed.
     */
    private inline fun <T> notClosed(block: () -> T): T {
        check(!closed) { "Manager is closed" }
        return block()
    }

    override fun close() {
        if (!closed) {
            clear()
            closed = true
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a stable [COpaquePointer] to [value].
 * Returns `null` if [value] is `null`.
 */
internal fun MemoryManager.managedPointer(value: Any?): COpaquePointer? {
    return value?.let(::managedPointer)
}

/**
 * Pins [value] and returns a [CPointer] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun MemoryManager.bufferPointer(value: ByteArray?): CPointer<ByteVar>? =
    value?.let(::bufferPointer)

/**
 * Allocates a copy of the [value] and returns a [CPointer] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun MemoryManager.stringPointer(value: String?): CPointer<ByteVar>? =
    value?.let(::stringPointer)
