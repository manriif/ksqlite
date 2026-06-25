package ksqlite.capi.types

import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.foreign.sqlite3.sqlite3_free
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout

/**
 * Base for output parameter.
 */
public abstract class OutputParamBase<Value> internal constructor(initialValue: Value) :
    OutputParam<Value> {

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
public abstract class PointerOutputParam<Value> : OutputParamBase<Value?>(null) {

    final override fun SegmentAllocator.allocate(initialValue: Value?): MemorySegment {
        check(initialValue == null)
        return allocate(ValueLayout.ADDRESS)
    }

    /**
     * Creates a new [Value] from non-null pointing [pointer].
     */
    protected abstract fun create(pointer: MemorySegment): Value

    final override fun readValue(segment: MemorySegment): Value? {
        return segment.get(ValueLayout.ADDRESS, 0).orNull?.let(::create)
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual open class Int32OutputParam actual constructor(initialValue: Int) :
    OutputParamBase<Int>(initialValue) {

    override fun SegmentAllocator.allocate(initialValue: Int): MemorySegment {
        return allocateFrom(ValueLayout.JAVA_INT, initialValue)
    }

    override fun readValue(segment: MemorySegment): Int {
        return segment.get(ValueLayout.JAVA_INT, 0)
    }
}

public actual class Int64OutputParam actual constructor(initialValue: Long) :
    OutputParamBase<Long>(initialValue) {

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

public actual class Utf8OutputParam actual constructor() : PointerOutputParam<String>() {

    private var freeOnRead: Boolean = false
    private var customSize: Int32OutputParam? = null

    override fun create(pointer: MemorySegment): String {
        val part = customSize?.value?.let { pointer.asSlice(0, it.toLong()) } ?: pointer
        val string = part.toKStringFromUtf8()

        if (freeOnRead) {
            sqlite3_free(pointer)
        }

        return string
    }

    /**
     * Updates [freeOnRead] and [customSize] properties during [block]'s execution.
     *
     * If [customSize] is not `null` then it is assumed that the strings that are read during
     * [block] execution are not zero terminated.
     *
     * If [freeOnRead] is `true` then the native string buffer is freed after it is read.
     */
    internal inline fun <R> overriding(
        freeOnRead: Boolean = false,
        customSize: Int32OutputParam? = null,
        block: () -> R
    ): R {
        this.freeOnRead = freeOnRead
        this.customSize = customSize

        return try {
            block()
        } finally {
            this.freeOnRead = false
            this.customSize = null
        }
    }
}

/**
 * Invokes [Utf8OutputParam.overriding] or returns [block]'s result if `this` is `null`.
 */
internal inline fun <R> Utf8OutputParam?.overriding(
    freeOnRead: Boolean = false,
    customSize: Int32OutputParam? = null,
    block: () -> R
): R = this?.overriding(freeOnRead, customSize, block) ?: block()

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class SqliteOutputParam actual constructor() :
    PointerOutputParam<sqlite3>() {

    override fun create(pointer: MemorySegment): sqlite3 {
        return sqlite3(pointer)
    }
}

public actual class SqliteBlobOutputParam actual constructor() :
    PointerOutputParam<sqlite3_blob>() {

    override fun create(pointer: MemorySegment): sqlite3_blob {
        return sqlite3_blob(pointer)
    }
}

public actual class SqliteSnapshotOutputParam actual constructor() :
    PointerOutputParam<sqlite3_snapshot>() {

    override fun create(pointer: MemorySegment): sqlite3_snapshot {
        return sqlite3_snapshot(pointer)
    }
}

public actual class SqliteStmtOutputParam actual constructor() :
    PointerOutputParam<sqlite3_stmt>() {

    override fun create(pointer: MemorySegment): sqlite3_stmt {
        return sqlite3_stmt(pointer)
    }
}

public actual class SqliteValueOutputParam actual constructor() :
    PointerOutputParam<sqlite3_value>() {

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
internal inline fun <R> OutputParamBase<*>.use(
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
    param: OutputParamBase<*>?,
    block: (MemorySegment) -> R
): R {
    if (param == null) {
        return block(NullPtr)
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
    param: OutputParamBase<*>?,
    block: (MemorySegment) -> R
): R {
    if (param == null) {
        return block(NullPtr)
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
    param1: OutputParamBase<*>?,
    param2: OutputParamBase<*>?,
    block: (
        pointer1: MemorySegment,
        pointer2: MemorySegment
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(NullPtr, NullPtr)
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
    param1: OutputParamBase<*>?,
    param2: OutputParamBase<*>?,
    block: (
        pointer1: MemorySegment,
        pointer2: MemorySegment
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(NullPtr, NullPtr)
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