package ksqlite.capi.memory

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pin
import kotlinx.cinterop.toLong
import ksqlite.capi.callbacks.SqliteDestroyCallback

internal actual class MemoryManager : MemoryManagerBase() {

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a stable [COpaquePointer] to [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can later be accessed using [stableRefDataHolder] and it can be
     * disposed using [stableRefDisposer].
     */
    private fun <AppData> commonStableRefPointer(
        key: String?,
        data: Any?,
        appData: AppData,
        destructor: SqliteDestroyCallback<AppData>?
    ): COpaquePointer? = notClosed {
        if (data == null && destructor == null) {
            key?.let(::clearDisposable)
            null
        } else {
            registerDisposable(key) { StableRefReference(it, destructor, data, appData) }
                .stableRef
                .asCPointer()
        }
    }

    /**
     * Returns a stable [COpaquePointer] to [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can be accessed using [stableRefDataHolder] and it can be
     * disposed using [stableRefDisposer].
     */
    fun <AppData> stableRefPointer(
        data: Any?,
        appData: AppData,
        destructor: SqliteDestroyCallback<AppData>? = null,
    ): COpaquePointer? = commonStableRefPointer(null, data, appData, destructor)

    /**
     * Returns a stable [COpaquePointer] to [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can be accessed using [stableRefDataHolder] and it can be
     * disposed using [stableRefDisposer].
     *
     * If a pointer was previously obtained using [key], it is disposed.
     */
    fun <AppData> keyedStableRefPointer(
        key: String,
        data: Any?,
        appData: AppData,
        destructor: SqliteDestroyCallback<AppData>? = null
    ): COpaquePointer? = commonStableRefPointer(key, data, appData, destructor)

    /**
     * Returns a [CPointer] to [value]'s content or `null` if [value] is `null`.
     *
     * The resulting disposable can be disposed using [globalDisposer].
     */
    internal fun byteArrayPointer(
        value: ByteArray,
        destructor: SqliteDestroyCallback<ByteArray>? = null
    ): CPointer<ByteVar> = notClosed {
        registerDisposable { ByteArrayDisposable(it, destructor, value) }.pointer
    }

    /**
     * Allocates a C-string with [value]'s content and returns a [CPointer] to the content.
     */
    fun keyedStringPointer(
        key: String,
        value: String
    ): CPointer<ByteVar> = notClosed {
        registerDisposable(key) { StringDisposable(it, value) }.pointer
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposables
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reference to [data].
     */
    private inner class StableRefReference<AppData>(
        id: Long,
        destructor: SqliteDestroyCallback<AppData>?,
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
        destructor: SqliteDestroyCallback<ByteArray>?,
        override val appData: ByteArray
    ) : AutoDisposable<ByteArray>(id, destructor) {

        private val pinned = appData.pin()
        val pointer = pinned.addressOf(0)

        init {
            registerGlobalDisposable(pointer.toLong(), this)
        }

        override fun release() {
            unregisterGlobalDisposable(pointer.toLong())
            pinned.unpin()
        }
    }

    /**
     * Reference to [String].
     */
    private inner class StringDisposable(
        id: Long,
        override val appData: String,
    ) : AutoDisposable<String>(id, null) {

        private val arena = Arena()
        val pointer = appData.cstr.getPointer(arena)

        override fun release() {
            arena.clear()
        }
    }
}