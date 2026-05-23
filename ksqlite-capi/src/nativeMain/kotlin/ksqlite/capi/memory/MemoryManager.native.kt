package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import ksqlite.capi.callbacks.Sqlite3DestroyCallback

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
    fun <AppData> stableRefPointer(
        data: Any?,
        appData: AppData,
        destructor: Sqlite3DestroyCallback<AppData>? = null,
    ): COpaquePointer? = notClosed {
        if (data == null && destructor == null) {
            return null
        }

        val reference = registerDisposable { id ->
            StableRefReference(id, destructor, data, appData)
        }

        return reference.stableRef.asCPointer()
    }

    /**
     * Returns a stable [COpaquePointer] to [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The resulting reference data can be accessed using [stableRefData] and it can be disposed
     * using [stableRefDisposer].
     *
     * If a pointer was previously obtained using [key], it is disposed.
     */
    fun <AppData> keyedStableRefPointer(
        key: String,
        data: Any?,
        appData: AppData,
        destructor: Sqlite3DestroyCallback<AppData>? = null
    ): COpaquePointer? = notClosed {
        if (data == null && destructor == null) {
            return null
        }

        val reference = registerKeyedDisposable(key) { id ->
            StableRefReference(id, destructor, data, appData)
        }

        return reference.stableRef.asCPointer()
    }

    /**
     * Returns a [CPointer] to [value]'s content or `null` if [value] is `null`.
     *
     * The resulting disposable can be disposed using [globalDisposer].
     */
    internal fun byteArrayPointer(
        value: ByteArray,
        destructor: Sqlite3DestroyCallback<ByteArray>? = null
    ): CPointer<ByteVar> = notClosed {
        val pinned = value.pin()
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
    private inner class StableRefReference<AppData>(
        id: Long,
        destructor: Sqlite3DestroyCallback<AppData>?,
        override val data: Any?,
        override val appData: AppData
    ) : AutoDisposable<AppData>(id, destructor),
        Reference<AppData> {

        val stableRef = StableRef.create(this)

        override fun release() {
            stableRef.dispose()
        }
    }

    /**
     * Reference to [ByteArray].
     */
    private inner class ByteArrayDisposable(
        id: Long,
        destructor: Sqlite3DestroyCallback<ByteArray>?,
        private val pinned: Pinned<ByteArray>,
        private val pointer: COpaquePointer,
    ) : AutoDisposable<ByteArray>(id, destructor) {

        override val appData: ByteArray
            get() = pinned.get()

        override fun release() {
            unregisterGlobalDisposable(pointer)
            pinned.unpin()
        }
    }
}