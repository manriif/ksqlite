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
import ksqlite.types.Sqlite3DestructorCallback

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
    private inner class ManagedPointerImpl(
        private val value: Any?,
        private val destructor: Sqlite3DestructorCallback?
    ) : ManagedPointer {

        val stableRef = StableRef.create(this)

        @Suppress("UNCHECKED_CAST")
        override fun <Data : Any> get(): Data {
            checkNotNull(value)
            return value as Data
        }

        override fun dispose() {
            check(pointers.remove(this)) { "Pointer is not managed" }
            disposeInternal()
        }

        /**
         * Disposes without removing from [pointers].
         */
        fun disposeInternal() {
            destructor?.invoke()
            stableRef.dispose()
        }
    }

    /**
     * Returns a stable [COpaquePointer] to [value].
     * Returns `null` if both [value] and [destructor] are `null`.
     */
    internal fun managedPointer(
        value: Any?,
        destructor: Sqlite3DestructorCallback? = null
    ): COpaquePointer? = notClosed {
        if (value == null && destructor == null) {
            return null
        }

        val managed = ManagedPointerImpl(value, destructor)

        if (::pointers.isInitialized) {
            pointers.add(managed)
        } else {
            pointers = mutableSetOf(managed)
        }

        managed.stableRef.asCPointer()
    }

    /**
     * Returns a [CPointer] to [value]'s content.
     * Returns `null` if [value] is `null`.
     */
    internal fun bufferPointer(value: ByteArray?): CPointer<ByteVar>? = notClosed {
        val pinned = value?.pin() ?: return null

        if (::pinneds.isInitialized) {
            pinneds.add(pinned)
        } else {
            pinneds = mutableListOf(pinned)
        }

        pinned.addressOf(0)
    }

    /**
     * Allocates a copy of the [value] and returns a [CPointer] to the content.
     * Returns `null` if [value] is `null`.
     */
    internal fun stringPointer(value: String?): CPointer<ByteVar>? = notClosed {
        if (value == null) {
            return null
        }

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
            pointers.onEach(ManagedPointerImpl::disposeInternal).clear()
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