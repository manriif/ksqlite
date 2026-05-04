package ksqlite.capi.memory

import ksqlite.capi.handlers.Handler
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import kotlin.Any
import kotlin.ByteArray
import kotlin.NullPointerException
import kotlin.String
import kotlin.ULong
import kotlin.Unit
import kotlin.apply
import kotlin.getValue
import kotlin.lazy
import kotlin.reflect.KClass
import kotlin.text.String
import kotlin.toULong

internal actual class MemoryManager : MemoryManagerBase() {

    private val functionPointers by lazy { ConcurrentHashMap<KClass<*>, MemorySegment>() }

    private val stableRefDisposer: MemorySegment by lazy {
        functionPointer(::StableRefDisposerHandler)
    }

    ///////////////////////////////////////////////////////////////////////////
    // References
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the object, previously referenced using [stableRefPointer] and identified by
     * [pointer]'s address.
     *
     * @throws NullPointerException if there is no object associated with [pointer].
     */
    fun getStableRef(pointer: MemorySegment): Reference = notClosed {
        val refId = pointer.address().toULong()
        val reference = getDisposable<StableRefReference>(refId)
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
        destructor: Sqlite3DestructorCallback? = null
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
     * The resulting reference data can be accessed using [getStableRef] and it can be
     * disposed using [stableRefDisposer].
     */
    fun stableRefPointer(
        data: Any?,
        userData: sqlite3_mutable_pointer? = null,
        destructor: Sqlite3DestructorCallback? = null,
        key: String? = null
    ): MemorySegment = notClosed {
        if (data == null && destructor == null) {
            return MemorySegment.NULL
        }

        val reference = registerDisposable(key) { id ->
            StableRefReference(id, destructor, data, userData)
        }

        return reference.pointer
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    private fun getOrCreateFunctionPointer(
        handlerKlass: KClass<out Handler>,
        factory: (MemoryManager) -> Handler
    ): MemorySegment = notClosed {
        functionPointers.computeIfAbsent(handlerKlass) {
            val handler = factory(this)
            val functionDescriptor = handler.createFunctionDescriptor()

            val methodHandle = MethodHandles
                .lookup()
                .findVirtual(handler::class.java, "handle", functionDescriptor.toMethodType())
                .bindTo(handler)

            val reference = registerDisposable { id ->
                ArenaDisposable(id, handler)
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
    inline fun <reified H: Handler> functionPointer(
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
    inline fun <reified H: Handler> functionPointer(
        noinline factory: (MemoryManager) -> H
    ): MemorySegment {
        return functionPointer(this, factory)
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     *
     * This should preferably be used if there is no option to copy [value]'s content on native
     * side.
     *
     * Returns [MemorySegment.NULL] if [value] is `null`.
     */
    fun byteArrayPointer(
        value: ByteArray?,
        destructor: Sqlite3DestructorCallback? = null
    ): MemorySegment = notClosed {
        if (value == null) {
            return MemorySegment.NULL
        }

        val disposable = registerDisposable { id ->
            ByteArrayDisposable(id, value, destructor)
        }

        registerGlobalDisposable(disposable.pointer, disposable)
        return disposable.pointer
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     *
     * Returns [MemorySegment.NULL] if [value] is `null`.
     */
    fun stringPointer(
        value: String?,
        destructor: Sqlite3DestructorCallback? = null
    ): MemorySegment = notClosed {
        if (value == null) {
            return MemorySegment.NULL
        }

        val disposable = registerDisposable { id ->
            StringDisposable(id, value, destructor)
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
    private class StableRefDisposerHandler(manager: MemoryManager) : Handler(manager) {

        override fun createFunctionDescriptor(): FunctionDescriptor {
            return FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        }

        fun handle(userPtr: MemorySegment) {
            manager.getStableRef(userPtr).dispose()
        }
    }

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

        val pointer: MemorySegment = MemorySegment.ofAddress(id.toLong())

        override fun release() = Unit
    }

    /**
     * Reference to an [arena].
     */
    private open inner class ArenaDisposable(
        id: ULong,
        open val refValue: Any?,
        destructor: Sqlite3DestructorCallback? = null
    ) : AutoDisposable(id, destructor) {

        val arena: Arena = Arena.ofShared()

        override val userData: sqlite3_mutable_pointer?
            get() = null

        override fun release() {
            arena.close()
        }
    }

    /**
     * Reference to [ByteArray].
     */
    private inner class ByteArrayDisposable(
        id: ULong,
        refValue: ByteArray,
        destructor: Sqlite3DestructorCallback?,
    ) : ArenaDisposable(id, refValue, destructor) {

        val pointer: MemorySegment = arena.allocate(refValue.size.toLong()).apply {
            copyFrom(MemorySegment.ofArray(refValue))
        }

        override val userData: sqlite3_mutable_pointer? by lazy {
            sqlite3_mutable_pointer.from(pointer, pointer.byteSize())
        }

        override fun release() {
            super.release()
            unregisterGlobalDisposable(pointer)
        }
    }

    /**
     * Reference to [String].
     */
    private inner class StringDisposable(
        id: ULong,
        refValue: String,
        destructor: Sqlite3DestructorCallback?,
    ) : ArenaDisposable(id, destructor) {

        val pointer: MemorySegment = arena.allocateFrom(refValue)

        override val userData: sqlite3_mutable_pointer? by lazy {
            sqlite3_mutable_pointer.from(pointer, pointer.byteSize())
        }

        override fun release() {
            super.release()
            unregisterGlobalDisposable(pointer)
        }
    }
}