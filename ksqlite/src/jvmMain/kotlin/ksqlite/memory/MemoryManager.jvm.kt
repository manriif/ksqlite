package ksqlite.memory

import ksqlite.handlers.DestructorHandler
import ksqlite.handlers.Handler
import ksqlite.types.Sqlite3DestructorCallback
import ksqlite.types.Sqlite3Param
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandles

/**
 * Manages memory.
 */
public open class MemoryManager internal constructor() : AutoCloseable {

    private lateinit var disposables: MutableMap<ULong, Disposable>
    private var nextReferenceId: ULong = ULong.MIN_VALUE
    private var arena: Arena? = null
    private var _destructorFunctionPointer: MemorySegment? = null
    private var closed = false

    internal val destructorFunctionPointer: MemorySegment
        get() {
            if (_destructorFunctionPointer == null) {
                _destructorFunctionPointer = functionPointer(::DestructorHandler)
            }

            return checkNotNull(_destructorFunctionPointer)
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
     * Creates a reference to [disposable] and returns the reference identifier.
     */
    @IgnorableReturnValue
    private fun createReference(disposable: Disposable): ULong {
        val referenceId = ++nextReferenceId
        check(referenceId !in disposables) { "Too many managed references" }

        if (::disposables.isInitialized) {
            disposables[referenceId] = disposable
        } else {
            disposables = mutableMapOf(referenceId to disposable)
        }

        return referenceId
    }

    /**
     * Returns the object referenced by [segment]'s address.
     * Throws [IllegalStateException] if there is no object associated with [segment].
     */
    internal operator fun <Data> get(segment: MemorySegment): Data = notClosed {
        val referenceId = segment.address().toULong()

        val reference = checkNotNull(disposables[referenceId]) {
            "No object was referenced with id $referenceId"
        }

        @Suppress("UNCHECKED_CAST")
        checkNotNull(reference as? Reference).value as Data
    }

    /**
     * Disposes the  object designed by [segment]'s address making it available to GC.
     */
    internal fun dispose(segment: MemorySegment): Unit = notClosed {
        val referenceId = segment.address().toULong()
        disposables.remove(referenceId)?.dispose()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a strong reference to [value] preventing it from GC collection and returns a
     * [MemorySegment] that can be used to retrieve [value] using [get].
     *
     * Returns [MemorySegment.NULL] if both [value] and [destructor] are `null`.
     */
    internal fun referencePointer(
        value: Any?,
        destructor: Sqlite3DestructorCallback? = null
    ): MemorySegment = notClosed {
        if (value == null && destructor == null) {
            return MemorySegment.NULL
        }

        MemorySegment.ofAddress(createReference(Reference(value, destructor)).toLong())
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     *
     * Returns [MemorySegment.NULL] if [factory] is `null`.
     */
    internal fun functionPointer(
        factory: ((MemoryManager) -> Handler)?
    ): MemorySegment = notClosed {
        segment(factory) { factory ->
            val handler = factory(this)
            val functionDescriptor = handler.createFunctionDescriptor()

            val methodHandle = MethodHandles
                .lookup()
                .findVirtual(handler::class.java, "handle", functionDescriptor.toMethodType())
                .bindTo(handler)

            createReference(HandlerHolder(handler))

            withArena {
                Linker
                    .nativeLinker()
                    .upcallStub(methodHandle, functionDescriptor, this)
            }
        }
    }

    /**
     * Attaches the [param] and returns a [MemorySegment] the parameter value.
     *
     * Returns [MemorySegment.NULL] if [param] is `null`.
     */
    internal fun paramPointer(param: Sqlite3Param<*>?): MemorySegment = notClosed {
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
    internal fun bufferPointer(value: ByteArray?): MemorySegment = notClosed {
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
    internal fun stringPointer(value: String?): MemorySegment = notClosed {
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
    internal fun clear() {
        if (::disposables.isInitialized) {
            disposables.onEach { it.value.dispose() }.clear()
        }

        arena?.let { instance ->
            arena = null
            instance.close()
        }

        nextReferenceId = ULong.MIN_VALUE
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
            closed = true
            clear()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposable
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hold [handler].
     * Does nothing but required to keep [handler] away from GC.
     */
    private class HandlerHolder(handler: Handler) : Disposable {

        private var handler: Handler? = handler

        override fun dispose() {
            handler = null
        }
    }

    /**
     * Detaches [param] in disposing.
     */
    private class ParamDetacher(private val param: Sqlite3Param<*>) : Disposable {

        override fun dispose() {
            param.detach()
        }
    }

    /**
     * Hold [value] and invoke [destructor] on disposing.
     */
    private class Reference(
        val value: Any?,
        private val destructor: Sqlite3DestructorCallback?
    ) : Disposable {

        override fun dispose() {
            destructor?.invoke()
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a pointer to a static function that will invoke the `handle` function of the  [Handler]
 * returned by [factory].
 * 
 * Returns [MemorySegment.NULL] if [callback] is `null`.
 */
internal fun MemoryManager.functionPointer(
    callback: Any?,
    factory: (MemoryManager) -> Handler
): MemorySegment = functionPointer(factory.takeIf { callback != null })