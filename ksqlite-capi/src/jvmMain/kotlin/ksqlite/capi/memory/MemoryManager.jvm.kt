package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestructorCallback
import ksqlite.capi.handlers.Handler
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

internal actual class MemoryManager : MemoryManagerBase() {

    private val functionPointers by lazy { ConcurrentHashMap<KClass<*>, MemorySegment>() }
    private val stableRefDisposer by lazy { functionPointer(::StableRefDisposerHandler) }

    ///////////////////////////////////////////////////////////////////////////
    // References
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the object, previously referenced using [stableRefPointer] and identified by
     * [pointer]'s address.
     *
     * @throws NullPointerException if there is no object associated with [pointer].
     */
    fun <ClientData> getStableRef(pointer: MemorySegment): Reference<ClientData> = notClosed {
        val refId = pointer.address()
        val reference = getDisposable<ClientData, StableRefReference<ClientData>>(refId)
        return reference
    }

    /**
     * Returns a pointer to a function that disposes the reference previously obtained from
     * [stableRefPointer].
     *
     * Returns [MemorySegment.NULL] if both [data] and [destructor] are `null`
     */
    fun stableRefDisposer(
        data: Any?,
        destructor: Sqlite3DestructorCallback<*>? = null
    ): MemorySegment {
        return stableRefDisposer.takeIf { data != null || destructor != null }
            ?: MemorySegment.NULL
    }

    override fun clear() {
        super.clear()
        functionPointers.clear()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a [MemorySegment] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The resulting reference data can be accessed using [getStableRef] and it can be disposed
     * using [stableRefDisposer].
     */
    fun <ClientData> stableRefPointer(
        data: Any?,
        clientData: ClientData,
        destructor: Sqlite3DestructorCallback<ClientData>? = null
    ): MemorySegment = notClosed {
        if (data == null && destructor == null) {
            return MemorySegment.NULL
        }

        val reference = registerDisposable { id ->
            StableRefReference(id, destructor, data, clientData)
        }

        return reference.pointer
    }

    /**
     * Returns a [MemorySegment] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The resulting reference data can be accessed using [getStableRef] and it can be
     * disposed using [stableRefDisposer].
     * 
     * If a pointer was previously obtained using [key], it is disposed.
     */
    fun <ClientData> keyedStableRefPointer(
        key: String,
        data: Any?,
        clientData: ClientData,
        destructor: Sqlite3DestructorCallback<ClientData>? = null,
        old: ((Any?) -> Unit)? = null
    ): MemorySegment = notClosed {
        if (data == null && destructor == null) {
            return MemorySegment.NULL
        }

        val (reference, oldClientData) = registerKeyedDisposable(key) { id ->
            StableRefReference(id, destructor, data, clientData)
        }

        if (oldClientData != null && old != null) {
            old.invoke(oldClientData)
        }

        return reference.pointer
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    private fun getOrCreateFunctionPointer(
        handlerKlass: KClass<out Handler<*>>,
        factory: (MemoryManager) -> Handler<*>
    ): MemorySegment = notClosed {
        functionPointers.computeIfAbsent(handlerKlass) {
            val handler = factory(this)
            val functionDescriptor = handler.createFunctionDescriptor()

            val methodHandle = MethodHandles
                .lookup()
                .findVirtual(handler::class.java, "handle", functionDescriptor.toMethodType())
                .bindTo(handler)

            val reference = registerDisposable { id ->
                FunctionDisposable(id, handler)
            }

            Linker
                .nativeLinker()
                .upcallStub(methodHandle, functionDescriptor, reference.arena)
        }
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     *
     * Returns [MemorySegment.NULL] if [callback] is `null`.
     */
    inline fun <reified H : Handler<*>> functionPointer(
        callback: Any?,
        noinline factory: (MemoryManager) -> H
    ): MemorySegment = notClosed {
        if (callback == null) {
            return MemorySegment.NULL
        }

        return getOrCreateFunctionPointer(H::class, factory)
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    inline fun <reified H : Handler<*>> functionPointer(
        noinline factory: (MemoryManager) -> H
    ): MemorySegment {
        return functionPointer(this, factory)
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     *
     * This should preferably be used if there is no option to copy [value]'s content on native
     * side.
     */
    fun byteArrayPointer(
        value: ByteArray,
        destructor: Sqlite3DestructorCallback<ByteArray>?
    ): MemorySegment = notClosed {
        val disposable = registerDisposable { id ->
            ByteArrayDisposable(id, value, destructor)
        }

        registerGlobalDisposable(disposable.pointer, disposable)
        return disposable.pointer
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposables
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Handler that dispose reference to object to make it available for GC.
     */
    private class StableRefDisposerHandler(manager: MemoryManager) : Handler<Nothing>(manager) {

        override fun createFunctionDescriptor(): FunctionDescriptor {
            return FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        }

        fun handle(userPtr: MemorySegment) {
            manager.getStableRef<Nothing>(userPtr).dispose()
        }
    }

    /**
     * Reference to [data].
     */
    private inner class StableRefReference<ClientData>(
        id: Long,
        destructor: Sqlite3DestructorCallback<ClientData>?,
        override val data: Any?,
        override val clientData: ClientData
    ) : AutoDisposable<ClientData>(id, destructor),
        Reference<ClientData> {

        val pointer: MemorySegment = MemorySegment.ofAddress(id)

        override fun release() = Unit
    }

    /**
     * Reference to an [arena].
     */
    private abstract inner class ArenaDisposable<ClientData>(
        id: Long,
        destructor: Sqlite3DestructorCallback<ClientData>? = null
    ) : AutoDisposable<ClientData>(id, destructor) {

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
        override val clientData: Handler<*>,
    ) : ArenaDisposable<Handler<*>>(id, null)

    /**
     * Reference to [ByteArray].
     */
    private inner class ByteArrayDisposable(
        id: Long,
        override val clientData: ByteArray,
        destructor: Sqlite3DestructorCallback<ByteArray>?,
    ) : ArenaDisposable<ByteArray>(id, destructor) {

        // TODO see of copy is necessary
        val pointer: MemorySegment = arena.allocate(clientData.size.toLong()).apply {
            copyFrom(MemorySegment.ofArray(clientData))
        }

        override fun release() {
            super.release()
            unregisterGlobalDisposable(pointer)
        }
    }
}