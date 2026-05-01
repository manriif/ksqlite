package ksqlite.capi.memory

import ksqlite.capi.handlers.DisposerHandler
import ksqlite.capi.handlers.Handler
import ksqlite.capi.memory.MemoryManager.ArenaDisposable
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3OutParamBase
import ksqlite.capi.types.sqlite3_mutable_pointer
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandles

internal actual class MemoryManager : MemoryManagerBase() {

    /**
     * Pointer to the function that disposes the reference that the pointer passed to it points to.
     * Only reference created by this [MemoryManager] can be disposed.
     */
    val disposer: MemorySegment by lazy {
        functionPointer(this, ::DisposerHandler)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Arena
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Invokes [block] with the arena.
     */
    private fun <R> withArena(block: Arena.() -> R): R {
        val arena = this.arena ?: run {
            Arena.ofShared().also { instance ->
                this.arena = instance
            }
        }

        return arena.block()
    }

    ///////////////////////////////////////////////////////////////////////////
    // References
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the object, previously referenced using [strongRefPointer] and identified by
     * [pointer]'s address.
     *
     * @throws NullPointerException if there is no object associated with [pointer].
     */
    fun getStrongReference(pointer: MemorySegment): Reference {
        val refId = pointer.address().toULong()
        val reference = getDisposable<StrongRefReference>(refId)
        return reference
    }


    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a [MemorySegment] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The resulting reference data can be accessed using [getStrongReference] and it can be
     * disposed using [disposer].
     */
    fun strongRefPointer(
        data: Any?,
        userData: sqlite3_mutable_pointer? = null,
        destructor: Sqlite3DestructorCallback? = null,
        key: String? = null
    ): MemorySegment = notClosed {
        if (data == null && destructor == null) {
            return MemorySegment.NULL
        }

        val reference = registerDisposable(key) { id ->
            StrongRefReference(id, destructor, data, userData)
        }

        return reference.address
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     *
     * Returns [MemorySegment.NULL] if [callback] is `null`.
     */
    fun functionPointer(
        callback: Any?,
        factory: (MemoryManager) -> Handler
    ): MemorySegment = notClosed {
        if (callback == null) {
            return MemorySegment.NULL
        }

        val handler = factory(this)
        val functionDescriptor = handler.createFunctionDescriptor()

        val methodHandle = MethodHandles
            .lookup()
            .findVirtual(handler::class.java, "handle", functionDescriptor.toMethodType())
            .bindTo(handler)

        val reference = registerDisposable(block = ::ArenaDisposable)

        return Linker
            .nativeLinker()
            .upcallStub(methodHandle, functionDescriptor, reference.arena)
    }

    /**
     * Attaches the [param] and returns a [MemorySegment] to the parameter value.
     *
     * Returns [MemorySegment.NULL] if [param] is `null`.
     */
    fun paramPointer(param: Sqlite3OutParamBase<*>?): MemorySegment = notClosed {
        segment(param) { param ->
            withArena {
                param.attach(this).also {
                    createReference(ParamDetacher(param))
                }
            }
        }
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     *
     * This should preferably be used if there is no option to copy [value]'s content on native
     * side.
     *
     * Returns [MemorySegment.NULL] if [value] is `null`.
     */
    fun byteArrayPointer(value: ByteArray?): MemorySegment = notClosed {
        segment(value) { value ->
            withArena {
                allocate(value.size.toLong()).apply {
                    copyFrom(MemorySegment.ofArray(value))
                }
            }
        }
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     *
     * Returns [MemorySegment.NULL] if [value] is `null`.
     */
    fun stringPointer(value: String?): MemorySegment = notClosed {
        segment(value) { value ->
            withArena {
                allocateFrom(value)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cleanup
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Clears all the allocated memory and releases all the referenced objects.
     */
    actual override fun clear() {
        if (::disposables.isInitialized) {
            disposables.onEach { it.value.dispose() }.clear()
        }

        arena?.let { instance ->
            arena = null
            instance.close()
        }

        nextReferenceId = ULong.MIN_VALUE
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposable
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Reference to [data].
     */
    private inner class StrongRefReference(
        id: ULong,
        destructor: Sqlite3DestructorCallback?,
        override val data: Any?,
        override val userData: sqlite3_mutable_pointer?
    ) : AutoDisposable(id, destructor),
        Reference {

        val address: MemorySegment = MemorySegment.ofAddress(id.toLong())

        override fun release() = Unit
    }

    /**
     * Reference to an [arena].
     */
    private inner class ArenaDisposable(id: ULong) : AutoDisposable(id, null) {

        val arena: Arena = Arena.ofShared()

        override val userData: sqlite3_mutable_pointer?
            get() = null

        override fun release() {
            arena.close()
        }
    }

    /**
     * Hold [handler].
     * Does nothing but required to keep [handler] away from GC.
     */
    private class HandlerHolder(handler: Handler) : Reference {

        private var handler: Handler? = handler

        override fun dispose() {
            handler = null
        }
    }

    /**
     * Detaches [param] in disposing.
     */
    private class ParamDetacher(private val param: Sqlite3OutParamBase<*>) :
        Reference {

        override fun dispose() {
            param.detach()
        }
    }

    /**
     * Hold [value] and invoke [destructor] on disposing.
     */
    private class Referencee(
        val value: Any?,
        private val destructor: Sqlite3DestructorCallback?
    ) : Reference {

        override fun dispose() {
            destructor?.invoke()
        }
    }
}