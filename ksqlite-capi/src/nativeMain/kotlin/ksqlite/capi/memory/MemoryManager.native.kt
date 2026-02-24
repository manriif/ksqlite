package ksqlite.capi.memory

import kotlinx.cinterop.Arena
import kotlinx.cinterop.AutofreeScope
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pin
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3ParamBase

internal actual class MemoryManager : AutoCloseable {

    private lateinit var disposables: MutableList<Disposable>
    private lateinit var arena: Arena
    private var closed = false

    private val placement: AutofreeScope
        get() {
            if (!::arena.isInitialized) {
                arena = Arena()
            }

            return arena
        }

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a stable [COpaquePointer] to [value].
     * Returns `null` if both [value] and [destructor] are `null`.
     */
    internal fun referencePointer(
        value: Any?,
        destructor: Sqlite3DestructorCallback? = null
    ): COpaquePointer? = notClosed {
        if (value == null && destructor == null) {
            return null
        }

        val reference = ReferenceImpl(value, destructor)
        addDisposable(reference)

        return reference.stableRef.asCPointer()
    }

    /**
     * Attaches the [param] and returns a [CPointer] to a [P] instance.
     * Returns `null` if [param] is `null`.
     */
    internal fun <P : CPointed> paramPointer(param: Sqlite3ParamBase<*, P>?): CPointer<P>? = notClosed {
        if (param == null) {
            return null
        }

        val pointer = param.attach(placement)
        val disposable = CallbackDisposable(param::detach)
        addDisposable(disposable)

        return pointer
    }

    /**
     * Returns a [CPointer] to [value]'s content.
     * Returns `null` if [value] is `null`.
     */
    internal fun bufferPointer(value: ByteArray?): CPointer<ByteVar>? = notClosed {
        val pinned = value?.pin() ?: return null
        val disposable = CallbackDisposable(pinned::unpin)
        addDisposable(disposable)

        return pinned.addressOf(0)
    }

    /**
     * Allocates a copy of the [value] and returns a [CPointer] to the content.
     * Returns `null` if [value] is `null`.
     */
    internal fun stringPointer(value: String?): CPointer<ByteVar>? = notClosed {
        if (value == null) {
            return null
        }

        value.cstr.getPointer(placement)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cleanup
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Clears all the allocated memory and releases all the pinned/referenced objects.
     */
    internal fun clear() = notClosed {
        if (::disposables.isInitialized) {
            disposables.onEach(Disposable::dispose).clear()
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

    actual override fun close() {
        if (!closed) {
            clear()
            closed = true
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposable
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds [disposable] to the objects that should be disposed on [clear].
     */
    private fun addDisposable(disposable: Disposable) {
        if (::disposables.isInitialized) {
            disposables.add(disposable)
        } else {
            disposables = mutableListOf(disposable)
        }
    }

    /**
     * Removes [disposable] from the objects that should be disposed on [clear] and disposes it.
     */
    private fun removeDisposable(disposable: Disposable) {
        check(disposables.remove(disposable)) { "Resource is not managed" }
        disposable.dispose()
    }

    /**
     * Disposable which call the given [dispose] function on dispose called.
     */
    private class CallbackDisposable(private val dispose: () -> Unit) : Disposable {

        override fun dispose() {
            dispose.invoke()
        }
    }

    /**
     * Implementation of [Reference].
     */
    private inner class ReferenceImpl(
        private val value: Any?,
        private val destructor: Sqlite3DestructorCallback?
    ) : Reference,
        Disposable {

        val stableRef = StableRef.create(this)

        @Suppress("UNCHECKED_CAST")
        override fun <Data : Any> get(): Data {
            checkNotNull(value)
            return value as Data
        }

        override fun release() {
            removeDisposable(this)
        }

        override fun dispose() {
            destructor?.invoke()
            stableRef.dispose()
        }
    }
}