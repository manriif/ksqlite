package ksqlite.capi.types

import org.sqlite.jni.capi.OutputPointer

///////////////////////////////////////////////////////////////////////////
// Param
///////////////////////////////////////////////////////////////////////////

/**
 * Base for output parameter.
 */
public abstract class OutputParamBase<Value, OutPtr>
internal constructor(initialValue: Value) : OutputParameter<Value> {

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
public abstract class PointerOutputParam<Value, OutPtr> :
    OutputParamBase<Value?, OutPtr>(null)

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual class IntOutputParam actual constructor(initialValue: Int) :
    OutputParamBase<Int, OutputPointer.Int32>(initialValue) {

    override fun allocate(initialValue: Int): OutputPointer.Int32 {
        return OutputPointer.Int32(initialValue)
    }

    override fun readValue(pointer: OutputPointer.Int32): Int {
        return pointer.get()
    }
}

public actual class LongOutputParam actual constructor(initialValue: Long) :
    OutputParamBase<Long, OutputPointer.Int64>(initialValue) {

    override fun allocate(initialValue: Long): OutputPointer.Int64 {
        return OutputPointer.Int64(initialValue)
    }

    override fun readValue(pointer: OutputPointer.Int64): Long {
        return pointer.get()
    }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

// TODO sealed string or bytes
public actual class Utf8OutputParam actual constructor() :
    PointerOutputParam<String, OutputPointer.String>() {

    /**
     * Custom size if not zero terminated.
     */
    internal var size: Int? = null

    override fun allocate(initialValue: String?): OutputPointer.String {
        return OutputPointer.String(initialValue)
    }

    override fun readValue(pointer: OutputPointer.String): String? {
        return pointer.get()
    }
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3OutputParam actual constructor() :
    PointerOutputParam<sqlite3, OutputPointer.sqlite3>() {

    override fun allocate(initialValue: sqlite3?): OutputPointer.sqlite3 {
        return OutputPointer.sqlite3()
    }

    override fun readValue(pointer: OutputPointer.sqlite3): sqlite3? {
        return pointer.take()?.let(::sqlite3)
    }
}

public actual class Sqlite3BlobOutputParam actual constructor() :
    PointerOutputParam<sqlite3_blob, OutputPointer.sqlite3_blob>() {

    override fun allocate(initialValue: sqlite3_blob?): OutputPointer.sqlite3_blob {
        return OutputPointer.sqlite3_blob()
    }

    override fun readValue(pointer: OutputPointer.sqlite3_blob): sqlite3_blob? {
        return pointer.take()?.let(::sqlite3_blob)
    }
}

public actual class Sqlite3SnapshotOutputParam actual constructor() :
    PointerOutputParam<sqlite3_snapshot, Nothing>() {

    override fun allocate(initialValue: sqlite3_snapshot?): Nothing {
        TODO()
    }

    override fun readValue(pointer: Nothing): sqlite3_snapshot? {
        TODO()
    }
}

public actual class Sqlite3StmtOutputParam actual constructor() :
    PointerOutputParam<sqlite3_stmt, OutputPointer.sqlite3_stmt>() {

    override fun allocate(initialValue: sqlite3_stmt?): OutputPointer.sqlite3_stmt {
        return OutputPointer.sqlite3_stmt()
    }

    override fun readValue(pointer: OutputPointer.sqlite3_stmt): sqlite3_stmt? {
        return pointer.take()?.let(::sqlite3_stmt)
    }
}

public actual class Sqlite3ValueOutputParam actual constructor() :
    PointerOutputParam<sqlite3_value, OutputPointer.sqlite3_value>() {

    override fun allocate(initialValue: sqlite3_value?): OutputPointer.sqlite3_value {
        return OutputPointer.sqlite3_value()
    }

    override fun readValue(pointer: OutputPointer.sqlite3_value): sqlite3_value? {
        return pointer.take()?.let(::sqlite3_value)
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
internal inline fun <OutPtr, R> OutputParamBase<*, OutPtr>.use(block: (OutPtr) -> R): R {
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
internal inline fun <OutPtr, R> useParam(
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
internal inline fun <OutPtr1, OutPtr2, R> useParams(
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