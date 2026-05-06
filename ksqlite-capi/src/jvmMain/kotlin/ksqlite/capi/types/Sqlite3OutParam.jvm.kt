package ksqlite.capi.types

import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.getStringUtf8
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.notNull
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
        check(initialValue == null)
        return allocate(ValueLayout.ADDRESS)
    }

    /**
     * Creates a new [Value] from non-null pointing [pointer].
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

public actual class Sqlite3Utf8OutParam actual constructor() :
    Sqlite3PointerOutParamBase<String>() {

    /**
     * Custom size if not zero terminated.
     */
    internal var size: Int? = null

    override fun create(pointer: MemorySegment): String {
        val size = size ?: return pointer.getStringUtf8()

        return pointer
            .asSlice(0, size.toLong())
            .getStringUtf8()
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

public actual class Sqlite3BlobOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_blob>() {

    override fun create(pointer: MemorySegment): sqlite3_blob {
        return sqlite3_blob(pointer)
    }
}

public actual class Sqlite3ContextOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_context>() {

    override fun create(pointer: MemorySegment): sqlite3_context {
        return sqlite3_context(pointer)
    }
}

public actual class Sqlite3SnapshotOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_snapshot>() {

    override fun create(pointer: MemorySegment): sqlite3_snapshot {
        return sqlite3_snapshot(pointer)
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

/**
 * Allocates native memory for [param] into `this` [SegmentAllocator], invokes [block] with a
 * pointer to it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> SegmentAllocator.useParam(
    param: Sqlite3OutParamBase<*>?,
    block: (MemorySegment) -> R
): R {
    if (param == null) {
        return block(MemorySegment.NULL)
    }

    return param.use(this, block)
}

/**
 * Allocates native memory for [param]  into a [SegmentAllocator], invokes [block] with a pointer to
 * it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> useParamMemScoped(
    param: Sqlite3OutParamBase<*>?,
    block: (MemorySegment) -> R
): R {
    if (param == null) {
        return block(MemorySegment.NULL)
    }

    return memScoped {
        param.use(this, block)
    }
}

/**
 * Allocates native memory to [param1] and [param2] into `this` [SegmentAllocator], invokes [block]
 * with pointers to them and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <R> SegmentAllocator.useParams(
    param1: Sqlite3OutParamBase<*>?,
    param2: Sqlite3OutParamBase<*>?,
    block: (
        pointer1: MemorySegment,
        pointer2: MemorySegment
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(MemorySegment.NULL, MemorySegment.NULL)
    }

    val pointer1 = param1?.attach(this).notNull
    val pointer2 = param2?.attach(this).notNull

    return try {
        block(pointer1, pointer2)
    } finally {
        param1?.detach(pointer1)
        param2?.detach(pointer2)
    }
}

/**
 * Allocates native memory to [param1] and [param2] into a [SegmentAllocator], invokes [block] with
 * pointers to them and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <R> useParamsMemScoped(
    param1: Sqlite3OutParamBase<*>?,
    param2: Sqlite3OutParamBase<*>?,
    block: (
        pointer1: MemorySegment,
        pointer2: MemorySegment
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(MemorySegment.NULL, MemorySegment.NULL)
    }

    return memScoped {
        val pointer1 = param1?.attach(this).notNull
        val pointer2 = param2?.attach(this).notNull

        try {
            block(pointer1, pointer2)
        } finally {
            param1?.detach(pointer1)
            param2?.detach(pointer2)
        }
    }
}