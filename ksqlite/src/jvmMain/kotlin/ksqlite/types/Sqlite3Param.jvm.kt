package ksqlite.types

import ksqlite.memory.segment
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout

/**
 * Base for output parameter.
 */
public abstract class Sqlite3Param<Type> internal constructor(initialValue: Type) {

    private var segment: MemorySegment? = null
    private var lastValue: Type = initialValue

    protected val currentValue: Type
        get() = segment?.value() ?: lastValue

    /**
     * Allocates memory and initializes with [value].
     */
    protected abstract fun SegmentAllocator.allocate(value: Type): MemorySegment

    /**
     * Reads the current value from [MemorySegment].
     */
    protected abstract fun MemorySegment.value(): Type

    /**
     * Allocates memory into [allocator] and returns the [MemorySegment] to the allocated memory.
     */
    internal fun attach(allocator: SegmentAllocator): MemorySegment {
        check(segment == null) { "Param is already attached" }
        val allocated = allocator.allocate(lastValue)
        segment = allocated
        return allocated
    }

    /**
     * Invalidates the previously allocated [MemorySegment].
     */
    internal fun detach() {
        lastValue = checkNotNull(segment) { "Param is not attached" }.value()
        segment = null
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) :
    Sqlite3Param<Int>(initialValue) {

    internal actual open val intValue: Int
        get() = currentValue

    override fun SegmentAllocator.allocate(value: Int): MemorySegment {
        return allocateFrom(ValueLayout.JAVA_INT, value)
    }

    override fun MemorySegment.value(): Int {
        return get(ValueLayout.JAVA_INT, 0)
    }
}

public actual class Sqlite3LongParam actual constructor(initialValue: Long) :
    Sqlite3Param<Long>(initialValue) {

    public actual val value: Long
        get() = currentValue

    override fun SegmentAllocator.allocate(value: Long): MemorySegment {
        return allocateFrom(ValueLayout.JAVA_LONG, value)
    }

    override fun MemorySegment.value(): Long {
        return get(ValueLayout.JAVA_LONG, 0)
    }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3Utf8Param actual constructor(initialValue: String?) :
    Sqlite3Param<String?>(initialValue) {

    public actual val value: String?
        get() = currentValue

    override fun SegmentAllocator.allocate(value: String?): MemorySegment {
        return allocate(ValueLayout.ADDRESS).apply {
            set(ValueLayout.ADDRESS, 0, segment(value) { value ->
                allocateFrom(value, Charsets.UTF_8)
            })
        }
    }

    override fun MemorySegment.value(): String? {
        return getString(0, Charsets.UTF_8)
    }
}