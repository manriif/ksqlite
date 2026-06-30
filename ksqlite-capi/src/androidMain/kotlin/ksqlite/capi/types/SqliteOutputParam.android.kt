package ksqlite.capi.types

import ksqlite.foreign.OutputPointer
import ksqlite.capi.memory.isNull

/**
 * Base for output parameter.
 */
public abstract class OutputParamBase<Value, OutPtr : OutputPointer<*>>
internal constructor(initialValue: Value) : OutputParam<Value> {

    private var actualValue: Value = initialValue

    final override val value: Value
        get() = actualValue

    /**
     * Allocates memory for [OutPtr] and initializes with [initialValue].
     */
    protected abstract fun allocate(initialValue: Value): OutPtr

    /**
     * Reads the [Value] of [OutPtr] pointed by [pointer].
     */
    protected abstract fun readValue(pointer: OutPtr): Value

    /**
     * Allocates memory and returns the allocated [OutPtr].
     */
    internal fun attach(): OutPtr {
        return allocate(actualValue)
    }

    /**
     * Extracts the value of the previously allocated [OutPtr] from [pointer].
     */
    internal fun detach(pointer: OutPtr) {
        actualValue = readValue(pointer)
    }
}

/**
 * Base for pointer output parameter.
 */
public abstract class PointerOutputParam<Value> :
    OutputParamBase<Value?, OutputPointer.OfPointer>(null) {

    final override fun allocate(initialValue: Value?): OutputPointer.OfPointer {
        return OutputPointer.OfPointer(0L)
    }

    /**
     * Creates a new [Value] from non-null pointing [pointer].
     */
    protected abstract fun create(pointer: Long): Value

    final override fun readValue(pointer: OutputPointer.OfPointer): Value? {
        return pointer.value.takeUnless(Long::isNull)?.let(::create)
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual class Int32OutputParam actual constructor(initialValue: Int) :
    OutputParamBase<Int, OutputPointer.OfInt32>(initialValue) {

    override fun allocate(initialValue: Int): OutputPointer.OfInt32 {
        return OutputPointer.OfInt32(initialValue)
    }

    override fun readValue(pointer: OutputPointer.OfInt32): Int {
        return pointer.value
    }
}

public actual class Int64OutputParam actual constructor(initialValue: Long) :
    OutputParamBase<Long, OutputPointer.OfInt64>(initialValue) {

    override fun allocate(initialValue: Long): OutputPointer.OfInt64 {
        return OutputPointer.OfInt64(initialValue)
    }

    override fun readValue(pointer: OutputPointer.OfInt64): Long {
        return pointer.value
    }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

public actual class Utf8OutputParam actual constructor() :
    OutputParamBase<String?, OutputPointer.OfString>(null) {

    override fun allocate(initialValue: String?): OutputPointer.OfString {
        ensurePointerInitialValueIsNull(initialValue)
        return OutputPointer.OfString(initialValue)
    }

    override fun readValue(pointer: OutputPointer.OfString): String? {
        return pointer.value
    }
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class SqliteOutputParam actual constructor() :
    PointerOutputParam<sqlite3>() {

    override fun create(pointer: Long): sqlite3 {
        return sqlite3(pointer)
    }
}

public actual class SqliteBlobOutputParam actual constructor() :
    PointerOutputParam<sqlite3_blob>() {

    override fun create(pointer: Long): sqlite3_blob {
        return sqlite3_blob(pointer)
    }
}

public actual class SqliteSnapshotOutputParam actual constructor() :
    PointerOutputParam<sqlite3_snapshot>() {

    override fun create(pointer: Long): sqlite3_snapshot {
        return sqlite3_snapshot(pointer)
    }
}

public actual class SqliteStmtOutputParam actual constructor() :
    PointerOutputParam<sqlite3_stmt>() {

    override fun create(pointer: Long): sqlite3_stmt {
        return sqlite3_stmt(pointer)
    }
}

public actual class SqliteValueOutputParam actual constructor() :
    PointerOutputParam<sqlite3_value>() {

    override fun create(pointer: Long): sqlite3_value {
        return sqlite3_value(pointer)
    }
}

public actual class SqliteVfsOutputParam actual constructor() :
    PointerOutputParam<sqlite3_vfs>() {

    override fun create(pointer: Long): sqlite3_vfs {
        return sqlite3_vfs(pointer)
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates [OutPtr], invokes [block] with it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <OutPtr : OutputPointer<*>, R> OutputParamBase<*, OutPtr>.use(
    block: (OutPtr) -> R
): R {
    val pointer = attach()

    val result = try {
        block(pointer)
    } finally {
        detach(pointer)
    }

    return result
}

/**
 * Allocates [OutPtr], invokes [block] with it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <OutPtr : OutputPointer<*>, R> useParam(
    param: OutputParamBase<*, OutPtr>?,
    block: (OutPtr?) -> R
): R {
    if (param == null) {
        return block(null)
    }

    return param.use(block)
}

/**
 * Allocates [OutPtr1] and [OutPtr2], invokes [block] with allocated [OutPtr1] and [OutPtr2] and
 * returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <OutPtr1 : OutputPointer<*>, OutPtr2 : OutputPointer<*>, R> useParams(
    param1: OutputParamBase<*, OutPtr1>?,
    param2: OutputParamBase<*, OutPtr2>?,
    block: (
        pointer1: OutPtr1?,
        pointer2: OutPtr2?
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(null, null)
    }

    val pointer1 = param1?.attach()
    val pointer2 = param2?.attach()

    return try {
        block(pointer1, pointer2)
    } finally {
        pointer1?.let(param1::detach)
        pointer2?.let(param2::detach)
    }
}