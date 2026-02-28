package ksqlite.capi.types

import ksqlite.capi.memory.isNull
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout

///////////////////////////////////////////////////////////////////////////
// Param
///////////////////////////////////////////////////////////////////////////

/**
 * Base for output parameter.
 */
public abstract class Sqlite3OutParamBase<Value> internal constructor(initialValue: Value) :
    Sqlite3OutParam<Value> {

    private var actualValue: Value = initialValue

    final override val value: Value
        get() = actualValue

    /**
     * Allocates memory and initializes with [initialValue].
     */
    protected abstract fun SegmentAllocator.allocate(initialValue: Value): MemorySegment

    /**
     * Reads the current [Value] from [segment].
     */
    protected abstract fun readValue(segment: MemorySegment): Value

    /**
     * Allocates memory into [allocator] and returns the [MemorySegment] to the allocated [Value].
     */
    internal fun attach(allocator: SegmentAllocator): MemorySegment {
        return allocator.allocate(actualValue)
    }

    /**
     * Extracts the value of the previously allocated [Value] from [segment].
     */
    internal fun detach(segment: MemorySegment) {
        actualValue = readValue(segment)
    }
}

/**
 * Base for pointer output parameter.
 */
public abstract class Sqlite3PointerOutParamBase<Value> : Sqlite3OutParamBase<Value?>(null) {

    final override fun SegmentAllocator.allocate(initialValue: Value?): MemorySegment {
        return allocate(ValueLayout.ADDRESS)
    }

    /**
     * Creates a new [Value] from non-null pointing [pointer]
     */
    protected abstract fun create(pointer: MemorySegment): Value

    final override fun readValue(segment: MemorySegment): Value? {
        if (segment.isNull) {
            return null
        }

        return create(segment)
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual open class Sqlite3IntOutParam actual constructor(initialValue: Int) :
    Sqlite3OutParamBase<Int>(initialValue) {

    override fun SegmentAllocator.allocate(initialValue: Int): MemorySegment {
        return allocateFrom(ValueLayout.JAVA_INT, initialValue)
    }

    override fun readValue(segment: MemorySegment): Int {
        return segment.get(ValueLayout.JAVA_INT, 0)
    }
}

public actual class Sqlite3LongOutParam actual constructor(initialValue: Long) :
    Sqlite3OutParamBase<Long>(initialValue) {

    override fun SegmentAllocator.allocate(initialValue: Long): MemorySegment {
        return allocateFrom(ValueLayout.JAVA_LONG, initialValue)
    }

    override fun readValue(segment: MemorySegment): Long {
        return segment.get(ValueLayout.JAVA_LONG, 0)
    }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3StringUtf8OutParam actual constructor() :
    Sqlite3PointerOutParamBase<String>() {

    override fun create(pointer: MemorySegment): String {
        return pointer.getString(0, Charsets.UTF_8)
    }
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3DatabaseConnectionOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3>() {

    override fun create(pointer: MemorySegment): sqlite3 {
        return sqlite3(pointer)
    }
}

public actual class Sqlite3ContextOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_context>() {

    override fun create(pointer: MemorySegment): sqlite3_context {
        return sqlite3_context(pointer)
    }
}

public actual class Sqlite3StatementOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_stmt>() {

    override fun create(pointer: MemorySegment): sqlite3_stmt {
        return sqlite3_stmt(pointer)
    }
}

public actual class Sqlite3ValueOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_value>() {

    override fun create(pointer: MemorySegment): sqlite3_value {
        return sqlite3_value(pointer)
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates value into [allocator], invokes [block] with a pointer to it and returns [block]'s
 * result.
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> Sqlite3OutParamBase<*>.use(
    allocator: SegmentAllocator,
    block: (MemorySegment) -> R
): R {
    val pointer = attach(allocator)

    val result = try {
        block(pointer)
    } finally {
        detach(pointer)
    }

    return result
}