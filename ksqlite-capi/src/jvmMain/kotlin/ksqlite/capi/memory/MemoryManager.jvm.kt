package ksqlite.capi.memory

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.handlers.Handler
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

internal actual class MemoryManager : MemoryManagerBase() {

    private val functionPointers = ConcurrentHashMap<KClass<*>, MemorySegment>()
    val stableRefDisposer by lazy { functionPointer(::StableRefDisposerHandler) }

    override fun clear() {
        super.clear()
        functionPointers.clear()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the object, previously referenced using [stableRefPointer] and identified by
     * [pointer]'s address.
     *
     * @throws NullPointerException if there is no object associated with [pointer].
     */
    fun <AppData> getStableRef(pointer: MemorySegment): Reference<AppData> = notClosed {
        val refId = pointer.address()
        val reference = getDisposable<AppData, StableRefReference<AppData>>(refId)
        return reference
    }

    /**
     * Returns a [MemorySegment] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can later be accessed using [getStableRef] and it can be disposed
     * using [stableRefDisposer].
     */
    private fun <AppData> commonStableRefPointer(
        key: String?,
        data: Any?,
        appData: AppData,
        destructor: SqliteDestroyCallback<AppData>?
    ): MemorySegment = notClosed {
        if (data == null && destructor == null) {
            key?.let(::clearDisposable)
            NullPtr
        } else {
            registerDisposable(key) { StableRefReference(it, destructor, data, appData) }.pointer
        }
    }

    /**
     * Returns a [MemorySegment] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can be accessed using [getStableRef] and it can be disposed
     * using [stableRefDisposer].
     */
    fun <AppData> stableRefPointer(
        data: Any?,
        appData: AppData,
        destructor: SqliteDestroyCallback<AppData>? = null
    ): MemorySegment = commonStableRefPointer(null, data, appData, destructor)

    /**
     * Returns a [MemorySegment] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can be accessed using [getStableRef] and it can be
     * disposed using [stableRefDisposer].
     *
     * If a pointer was previously obtained using [key], it is disposed.
     */
    fun <AppData> keyedStableRefPointer(
        key: String,
        data: Any?,
        appData: AppData,
        destructor: SqliteDestroyCallback<AppData>? = null
    ): MemorySegment = commonStableRefPointer(key, data, appData, destructor)

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    private fun getOrCreateFunctionPointer(
        handlerKlass: KClass<out Handler>,
        factory: () -> Handler
    ): MemorySegment = notClosed {
        functionPointers.computeIfAbsent(handlerKlass) {
            val handler = factory().apply {
                manager = this@MemoryManager
            }

            val reference = registerDisposable { id ->
                FunctionDisposable(id, handler)
            }

            handler.allocate(reference.arena)
        }
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     *
     * Returns [NullPtr] if [callback] is `null`.
     */
    inline fun <reified H : Handler> functionPointer(
        callback: Any?,
        noinline factory: () -> H
    ): MemorySegment = notClosed {
        if (callback == null) {
            return NullPtr
        }

        return getOrCreateFunctionPointer(H::class, factory)
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    inline fun <reified H : Handler> functionPointer(
        noinline factory: () -> H
    ): MemorySegment = functionPointer(this, factory)

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     *
     * The resulting disposable can be disposed using [globalDisposer].
     */
    fun byteArrayPointer(
        value: ByteArray,
        destructor: SqliteDestroyCallback<ByteArray>?
    ): MemorySegment = notClosed {
        registerDisposable { ByteArrayDisposable(it, destructor, value) }.pointer
    }

    /**
     * Allocates a C-string with [value]'s content and returns a [MemorySegment] to the content.
     */
    fun keyedStringPointer(
        key: String,
        value: String
    ): MemorySegment = notClosed {
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

        val pointer: MemorySegment = MemorySegment.ofAddress(id)

        override fun release() = Unit
    }

    /**
     * Reference to an [arena].
     */
    private abstract inner class ArenaDisposable<AppData>(
        id: Long,
        destructor: SqliteDestroyCallback<AppData>? = null
    ) : AutoDisposable<AppData>(id, destructor) {

        val arena: Arena = Arena.ofShared()

        override fun release() {
            arena.close()
        }
    }

    /**
     * Reference to [Handler].
     */
    private inner class FunctionDisposable(
        id: Long,
        override val appData: Handler,
    ) : ArenaDisposable<Handler>(id, null)

    /**
     * Reference to [ByteArray].
     */
    private inner class ByteArrayDisposable(
        id: Long,
        destructor: SqliteDestroyCallback<ByteArray>?,
        override val appData: ByteArray,
    ) : ArenaDisposable<ByteArray>(id, destructor) {

        val pointer: MemorySegment = arena.allocate(appData.size.toLong()).apply {
            copyFrom(MemorySegment.ofArray(appData))
        }

        init {
            registerGlobalDisposable(pointer.address(), this)
        }

        override fun release() {
            super.release()
            unregisterGlobalDisposable(pointer.address())
        }
    }

    /**
     * Reference to [String].
     */
    private inner class StringDisposable(
        id: Long,
        override val appData: String
    ) : ArenaDisposable<String>(id, null) {

        val pointer: MemorySegment = arena.allocateFrom(appData, Charsets.UTF_8)
    }
}