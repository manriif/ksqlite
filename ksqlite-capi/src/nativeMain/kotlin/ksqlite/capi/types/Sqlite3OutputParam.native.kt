package ksqlite.capi.types

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import ksqlite.capi.memory.toKStringFromUtf8

///////////////////////////////////////////////////////////////////////////
// Param
///////////////////////////////////////////////////////////////////////////

/**
 * Base for output parameter.
 */
public abstract class OutputParamBase<Value, Var : CPointed>
internal constructor(initialValue: Value) : OutputParameter<Value> {

    private var actualValue: Value = initialValue

    final override val value: Value
        get() = actualValue

    /**
     * Allocates memory for [Var] and initializes with [initialValue].
     */
    protected abstract fun NativePlacement.allocate(initialValue: Value): Var

    /**
     * Reads the [Value] of [Var] pointed by [pointer].
     */
    protected abstract fun readValue(pointer: CPointer<Var>): Value

    /**
     * Allocates memory into [placement] and returns the [CPointer] to the allocated [Var].
     */
    internal fun attach(placement: NativePlacement): CPointer<Var> {
        return placement.allocate(actualValue).ptr
    }

    /**
     * Extracts the value of the previously allocated [Var] from [pointer].
     */
    internal fun detach(pointer: CPointer<Var>) {
        actualValue = readValue(pointer)
    }
}

/**
 * Base for pointer output parameter.
 */
public abstract class PointerOutputParam<Value, Var : CPointed> :
    OutputParamBase<Value?, CPointerVar<Var>>(null) {

    final override fun NativePlacement.allocate(initialValue: Value?): CPointerVar<Var> {
        check(initialValue == null)
        return allocPointerTo()
    }

    /**
     * Creates a new [Value] instance from [pointer].
     */
    protected abstract fun create(pointer: CPointer<Var>): Value

    final override fun readValue(pointer: CPointer<CPointerVar<Var>>): Value? {
        return pointer.pointed.value?.let(::create)
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////


public actual class Int32OutputParam actual constructor(initialValue: Int) :
    OutputParamBase<Int, IntVar>(initialValue) {

    override fun NativePlacement.allocate(initialValue: Int): IntVar {
        return alloc(value)
    }

    override fun readValue(pointer: CPointer<IntVar>): Int {
        return pointer.pointed.value
    }
}

public actual class Int64OutputParam actual constructor(initialValue: Long) :
    OutputParamBase<Long, LongVar>(initialValue) {

    override fun NativePlacement.allocate(initialValue: Long): LongVar {
        return alloc(value)
    }

    override fun readValue(pointer: CPointer<LongVar>): Long {
        return pointer.pointed.value
    }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

public actual class Utf8OutputParam actual constructor() :
    PointerOutputParam<String, ByteVar>() {

    /**
     * Custom size if not zero terminated.
     */
    internal var size: Int? = null

    override fun create(pointer: CPointer<ByteVar>): String {
        return size?.let { pointer.toKStringFromUtf8(it) } ?: pointer.toKStringFromUtf8()
    }
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3OutputParam actual constructor() :
    PointerOutputParam<sqlite3, s3>() {

    override fun create(pointer: CPointer<s3>): sqlite3 {
        return sqlite3(pointer)
    }
}

public actual class Sqlite3BlobOutputParam actual constructor() :
    PointerOutputParam<sqlite3_blob, s3_blob>() {

    override fun create(pointer: CPointer<s3_blob>): sqlite3_blob {
        return sqlite3_blob(pointer)
    }
}

public actual class Sqlite3SnapshotOutputParam actual constructor() :
    PointerOutputParam<sqlite3_snapshot, s3_snapshot>() {

    override fun create(pointer: CPointer<s3_snapshot>): sqlite3_snapshot {
        return sqlite3_snapshot(pointer)
    }
}

public actual class Sqlite3StmtOutputParam actual constructor() :
    PointerOutputParam<sqlite3_stmt, s3_stmt>() {

    override fun create(pointer: CPointer<s3_stmt>): sqlite3_stmt {
        return sqlite3_stmt(pointer)
    }
}

public actual class Sqlite3ValueOutputParam actual constructor() :
    PointerOutputParam<sqlite3_value, s3_value>() {

    override fun create(pointer: CPointer<s3_value>): sqlite3_value {
        return sqlite3_value(pointer)
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates [Var] into [placement], invokes [block] with a pointer to it and returns [block]'s
 * result.
 * 
 * The pointer passed to [block] must not escape.
 */
internal inline fun <Var : CPointed, R> OutputParamBase<*, Var>.use(
    placement: NativePlacement,
    block: (CPointer<Var>) -> R
): R {
    val pointer = attach(placement)

    val result = try {
        block(pointer)
    } finally {
        detach(pointer)
    }

    return result
}

/**
 * Allocates [Var] into `this` [NativePlacement], invokes [block] with a pointer to [Var] and
 * returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <Var : CPointed, R> NativePlacement.useParam(
    param: OutputParamBase<*, Var>?,
    block: (CPointer<Var>?) -> R
): R {
    if (param == null) {
        return block(null)
    }

    return param.use(this, block)
}

/**
 * Allocates [Var] into a [kotlinx.cinterop.MemScope], invokes [block] with a pointer to [Var] and
 * returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <Var : CPointed, R> useParamMemScoped(
    param: OutputParamBase<*, Var>?,
    block: (CPointer<Var>?) -> R
): R {
    if (param == null) {
        return block(null)
    }

    return memScoped {
        param.use(this, block)
    }
}

/**
 * Allocates [Var1] and [Var2] into `this` [NativePlacement], invokes [block] with pointers to
 * [Var1] and [Var2] and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <Var1 : CPointed, Var2 : CPointed, R> NativePlacement.useParams(
    param1: OutputParamBase<*, Var1>?,
    param2: OutputParamBase<*, Var2>?,
    block: (
        pointer1: CPointer<Var1>?,
        pointer2: CPointer<Var2>?
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(null, null)
    }

    val pointer1 = param1?.attach(this)
    val pointer2 = param2?.attach(this)

    return try {
        block(pointer1, pointer2)
    } finally {
        pointer1?.let(param1::detach)
        pointer2?.let(param2::detach)
    }
}

/**
 * Allocates [Var1] and [Var2] into a [kotlinx.cinterop.MemScope], invokes [block] with pointers to
 * [Var1] and [Var2] and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <Var1 : CPointed, Var2 : CPointed, R> useParamsMemScoped(
    param1: OutputParamBase<*, Var1>?,
    param2: OutputParamBase<*, Var2>?,
    block: (
        pointer1: CPointer<Var1>?,
        pointer2: CPointer<Var2>?
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(null, null)
    }

    return memScoped {
        val pointer1 = param1?.attach(this)
        val pointer2 = param2?.attach(this)

        try {
            block(pointer1, pointer2)
        } finally {
            pointer1?.let(param1::detach)
            pointer2?.let(param2::detach)
        }
    }
}