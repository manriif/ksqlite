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
 * Manages memory.
 */
public open class MemoryManager internal constructor() : AutoCloseable {

    private lateinit var disposables: MutableList<KFunction0<Unit>>
    private lateinit var arena: Arena
    private var closed = false

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
    internal fun referencePointer(value: Any): COpaquePointer = notClosed {
        val stableRef = StableRef.create(value)
        addDisposable(stableRef::dispose)
        stableRef.asCPointer()
    }

    /**
     * Returns a [CPointer] to [value]'s content.
     */
    internal fun bufferPointer(value: ByteArray): CPointer<ByteVar> = notClosed {
        val pinned = value.pin()
        addDisposable(pinned::unpin)
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
     * Clears all the allocated memory and unpins all the pinneds objects.
     */
    internal fun clear() = notClosed {
        if (::arena.isInitialized) {
            arena.clear()
        }

        if (::disposables.isInitialized) {
            disposables.onEach(KFunction0<Unit>::invoke).clear()
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
 * Allocates a copy of the [value] and returns a [CPointer] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun MemoryManager.referencePointer(value: Any?): COpaquePointer? {
    return value?.let(::referencePointer)
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
