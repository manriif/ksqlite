package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import ksqlite.capi.callbacks.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

internal actual class MemoryManager : MemoryManagerBase() {

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a stable [COpaquePointer] to [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The resulting reference data can be accessed using [stableRefData] and it can be disposed
     * using [stableRefDisposer].
     */
    fun stableRefPointer(
        data: Any?,
        userData: sqlite3_mutable_pointer? = null,
        destructor: Sqlite3DestructorCallback? = null,
        key: String? = null
    ): COpaquePointer? = notClosed {
        if (data == null && destructor == null) {
            return null
        }

        val reference = registerDisposable(key) { id ->
            StableRefReference(id, destructor, data, userData)
        }

        return reference.stableRef.asCPointer()
    }

    /**
     * Returns a [CPointer] to [value]'s content or `null` if [value] is `null`.
     *
     * The resulting disposable can be disposed using [globalDisposer].
     */
    internal fun byteArrayPointer(
        value: ByteArray?,
        destructor: Sqlite3DestructorCallback? = null
    ): CPointer<ByteVar>? = notClosed {
        val pinned = value?.pin() ?: return null
        val pointer = pinned.addressOf(0)

        val disposable = registerDisposable { id ->
            ByteArrayDisposable(id, destructor, pinned, pointer)
        }

        registerGlobalDisposable(pointer, disposable)
        return pointer
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposables
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reference to [data].
     */
    private inner class StableRefReference(
        id: ULong,
        destructor: Sqlite3DestructorCallback?,
        override val data: Any?,
        override val userData: sqlite3_mutable_pointer?
    ) : AutoDisposable(id, destructor),
        Reference {

        val stableRef = StableRef.create(this)

        override fun release() {
            stableRef.dispose()
        }
    }

    /**
     * Reference to [ByteArray].
     */
    private inner class ByteArrayDisposable(
        id: ULong,
        destructor: Sqlite3DestructorCallback?,
        private val pinned: Pinned<ByteArray>,
        private val pointer: COpaquePointer,
    ) : AutoDisposable(id, destructor) {

        override val userData: sqlite3_mutable_pointer? by lazy {
            sqlite3_mutable_pointer.from(pointer, pinned.get().size.toLong())
        }

        override fun release() {
            unregisterGlobalDisposable(pointer)
            pinned.unpin()
        }
    }
}