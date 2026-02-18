package ksqlite.memory

import ksqlite.handlers.DestructorHandler
import ksqlite.handlers.Handler
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandles

/**
 * Manages memory.
 */
public open class MemoryManager internal constructor() : AutoCloseable {

    private lateinit var references: MutableMap<ULong, Any>
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
    // References
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a reference to [value] and returns the reference identifier.
     */
    @IgnorableReturnValue
    private fun createReference(value: Any): ULong {
        val referenceId = ++nextReferenceId
        check(referenceId !in references) { "Too many managed references" }

        if (::references.isInitialized) {
            references[referenceId] = value
        } else {
            references = mutableMapOf(referenceId to value)
        }

        return referenceId
    }

    /**
     * Returns the object referenced by [segment]'s address.
     */
    internal inline operator fun <reified Data> get(segment: MemorySegment): Data = notClosed {
        val referenceId = segment.address().toULong()

        checkNotNull(references[referenceId]) {
            "No object was referenced with id $referenceId"
        } as Data
    }

    /**
     * Clears the reference to the object designed by [segment]'s address making it available to GC.
     * Throws [IllegalStateException] there is no object associated with [segment].
     */
    @IgnorableReturnValue
    internal fun clear(segment: MemorySegment): Any = notClosed {
        val referenceId = segment.address().toULong()

        checkNotNull(references.remove(referenceId)) {
            "No object was referenced with id $referenceId"
        }
    }

    /**
     * Creates a strong reference to [value] preventing it from GC collection and returns a
     * [MemorySegment] that can be used to retrieve [value] using [get].
     */
    internal fun referencePointer(value: Any): MemorySegment = notClosed {
        MemorySegment.ofAddress(createReference(value).toLong())
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

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by factory.
     */
    internal fun functionPointer(factory: (MemoryManager) -> Handler): MemorySegment = notClosed {
        val handler = factory(this)
        val functionDescriptor = handler.createFunctionDescriptor()

        val methodHandle = MethodHandles
            .lookup()
            .findVirtual(handler::class.java, "handle", functionDescriptor.toMethodType())
            .bindTo(handler)

        createReference(handler)

        withArena {
            Linker
                .nativeLinker()
                .upcallStub(methodHandle, functionDescriptor, this)
        }
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     *
     * This should preferably be used if there is no option to copy [value]'s content on native
     * side.
     */
    internal fun bufferPointer(value: ByteArray): MemorySegment = notClosed {
        withArena {
            allocate(value.size.toLong()).apply {
                copyFrom(MemorySegment.ofArray(value))
            }
        }
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     */
    internal fun stringPointer(value: String): MemorySegment = notClosed {
        withArena {
            allocateFrom(value)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cleanup
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Clears all the allocated memory and releases all the referenced objects.
     */
    internal fun clear() {
        arena?.let { instance ->
            arena = null
            instance.close()
        }

        if (::references.isInitialized) {
            references.clear()
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
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the object referenced by [segment]'s address or `null` if the [segment] points to the
 * null address.
 */
internal inline fun <reified Data : Any> MemoryManager.getOrNull(segment: MemorySegment): Data? {
    if (segment.address() == MemorySegment.NULL.address()) {
        return null
    }

    return get(segment)
}

/**
 * Creates a strong reference to [value] preventing it from GC collection and returns a
 * [MemorySegment] that can be used to retrieve [value] using [getOrNull].
 */
internal fun MemoryManager.referencePointer(value: Any?): MemorySegment {
    return segment(value, this::referencePointer)
}

/**
 * Returns a pointer to a static function that will invoke the `handle` function of the
 * [Handler] returned by factory.
 */
internal fun MemoryManager.functionPointer(factory: ((MemoryManager) -> Handler)?): MemorySegment {
    return segment(factory, this::functionPointer)
}

/**
 * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
 * Returns [MemorySegment.NULL] if [value] is `null`
 *
 * This should preferably be used if there is no option to copy [value]'s content on native side.
 */
internal fun MemoryManager.bufferPointer(value: ByteArray?): MemorySegment {
    return segment(value, this::bufferPointer)
}

/**
 * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
 * Returns [MemorySegment.NULL] if [value] is `null`
 */
internal fun MemoryManager.stringPointer(value: String?): MemorySegment {
    return segment(value, this::stringPointer)
}
