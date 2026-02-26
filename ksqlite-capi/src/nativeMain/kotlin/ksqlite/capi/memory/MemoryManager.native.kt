package ksqlite.capi.memory

import kotlinx.cinterop.Arena
import kotlinx.cinterop.AutofreeScope
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pin
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

internal actual class MemoryManager : MemoryManagerBase() {

    private lateinit var disposables: MutableList<SelfDisposable>
    private lateinit var arena: Arena

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
     *
     * [value] can later be accessed within a callback using [userData] and disposed using
     * [refDisposer].
     */
    fun refPointer(
        value: Any?,
        userData: sqlite3_mutable_pointer? = null,
        destructor: Sqlite3DestructorCallback? = null
    ): COpaquePointer? = notClosed {
        if (value == null && destructor == null) {
            return null
        }

        val reference = ReferenceDisposable(value, destructor, userData)
        addDisposable(reference)

        return reference.stableRef.asCPointer()
    }

    /**
     * Returns a [CPointer] to [value]'s content.
     * Returns `null` if [value] is `null`.
     *
     * [value] can be disposed using [globalDisposer].
     */
    internal fun bufferPointer(value: ByteArray?): CPointer<ByteVar>? = notClosed {
        val pinned = value?.pin() ?: return null
        val address = pinned.addressOf(0)
        val disposable = PinnedDisposable(pinned, address)

        addDisposable(disposable)
        registerGlobalDisposable(address, disposable)

        return address
    }

    /**
     * Allocates a copy of the [value] and returns a [CPointer] to the content.
     * Returns `null` if [value] is `null`.
     */
    fun stringPointer(value: String?): CPointer<ByteVar>? = notClosed {
        if (value == null) {
            return null
        }

        return value.cstr.getPointer(placement)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cleanup
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Clears all the allocated memory and releases all the pinned/referenced objects.
     */
    actual override fun clear() = notClosed {
        if (::disposables.isInitialized) {
            disposables.onEach(SelfDisposable::release).clear()
        }

        if (::arena.isInitialized) {
            arena.clear()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposable
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds [disposable] to the objects that should be disposed on [clear].
     */
    private fun addDisposable(disposable: SelfDisposable) {
        if (::disposables.isInitialized) {
            disposables.add(disposable)
        } else {
            disposables = mutableListOf(disposable)
        }
    }

    /**
     * [Disposable] which can self removes from [disposables].
     * [release] should be used to make the actual disposing.
     */
    private abstract inner class SelfDisposable : Disposable {

        /**
         * Disposes the resource.
         */
        abstract fun release()

        /**
         * Removes `this` from the [disposables] and [release].
         */
        final override fun dispose() {
            check(disposables.remove(this)) { "Resource is not managed" }
            release()
        }
    }

    /**
     * Implementation of [Reference].
     */
    private inner class ReferenceDisposable(
        private val value: Any?,
        private val destructor: Sqlite3DestructorCallback?,
        override val userData: sqlite3_mutable_pointer?
    ) : Reference,
        SelfDisposable() {

        val stableRef = StableRef.create(this)

        @Suppress("UNCHECKED_CAST")
        override fun <Data : Any> get(): Data {
            checkNotNull(value)
            return value as Data
        }

        override fun release() {
            destructor?.invoke(userData)
            stableRef.dispose()
        }
    }

    /**
     * Unpins [pinned] on [dispose].
     */
    private inner class PinnedDisposable(
        private val pinned: Pinned<*>,
        private val pointer: COpaquePointer
    ) : SelfDisposable() {

        override fun release() {
            unregisterGlobalDisposable(pointer)
            pinned.unpin()
        }
    }
}