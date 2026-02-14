package ksqlite

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Provides [MemorySegment] to objects, managing their lifecycles.
 *
 * Callback function pointers and reference pointers stays in memory until it is cleared.
 * That said, referenced object can be made eligible to GC by calling [clear] with the associated
 * [MemorySegment].
 */
public open class PointerManager internal constructor() {

    private var arena: Arena? = null
    private lateinit var references: MutableMap<Long, Any>
    private var nextReferenceId: Long = Long.MIN_VALUE

    /**
     * Adds a reference to [value] and returns the reference identifier.
     */
    private fun addReference(value: Any): Long {
        val referenceId = nextReferenceId++
        check(referenceId !in references) { "Too much managed pointers" }

        if (::references.isInitialized) {
            references[referenceId] = value
        } else {
            references = mutableMapOf(referenceId to value)
        }

        return referenceId
    }

    /**
     * Invokes [block] with the arena.
     */
    private fun <R> withArena(block: Arena.() -> R): R {
        val arena = this.arena ?: run {
            // TODO maybe ofShared() can be better suited for coroutines, see thread limitation
            //  regarding SQLite
            Arena.ofConfined().also { instance ->
                this.arena = instance
            }
        }

        return arena.block()
    }

    /**
     * Returns a pointer to a static function that will invoke [function].
     */
    internal fun pointer(
        vararg argsLayout: ValueLayout,
        returnLayout: ValueLayout? = null,
        function: Function<*>
    ): MemorySegment = withArena {
        staticCFunction(
            arena = this,
            argsLayout = argsLayout,
            returnLayout = returnLayout,
            function = function
        )
    }

    /**
     * Returns a [MemorySegment] to [value]'s reference.
     */
    internal fun pointer(value: Any): MemorySegment = withArena {
        val referenceId = addReference(value)

        allocate(ValueLayout.JAVA_LONG).apply {
            set(ValueLayout.JAVA_LONG, 0, referenceId)
        }
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     * This should only be used if there is no option to copy [value]'s content on native side.
     */
    internal fun pointer(value: ByteArray): MemorySegment = withArena {
        allocate(value.size.toLong()).apply {
            copyFrom(MemorySegment.ofArray(value))
        }
    }

    /**
     * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
     */
    internal fun pointer(value: String): MemorySegment = withArena {
        allocateFrom(value)
    }

    /**
     * Clears the reference to the object designed by [segment] making it available to GC.
     * This does not release the memory for [segment].
     */
    public fun clear(segment: MemorySegment) {
        val referenceId = segment.get(ValueLayout.JAVA_LONG, 0)

        check(references.remove(referenceId) != null) {
            "No object was referenced with id $referenceId"
        }
    }

    /**
     * Clears all the allocated memory and unpins all the pinneds objects.
     */
    public fun clear() {
        arena?.let { instance ->
            arena = null
            instance.close()
        }

        if (::references.isInitialized) {
            references.clear()
        }

        nextReferenceId = Long.MIN_VALUE
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a pointer to a static function that will invoke [function].
 */
internal fun PointerManager.pointer(
    vararg argsLayout: ValueLayout,
    returnLayout: ValueLayout? = null,
    function: Function<*>?
): MemorySegment =
    segment(function) { pointer(*argsLayout, returnLayout = returnLayout, function = it) }

/**
 * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun PointerManager.pointer(value: Any?): MemorySegment =
    segment(value, this::pointer)

/**
 * Pins [value] and returns a [MemorySegment] to the content.
 * Returns `null` if [value] is `null`.
 *
 * This should only be used if there is no option to copy [value]'s content on native side.
 */
internal fun PointerManager.pointer(value: ByteArray?): MemorySegment =
    segment(value, this::pointer)

/**
 * Allocates a copy of the [value] and returns a [MemorySegment] to the content.
 * Returns `null` if [value] is `null`.
 */
internal fun PointerManager.pointer(value: String?): MemorySegment =
    segment(value, this::pointer)
