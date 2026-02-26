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

///////////////////////////////////////////////////////////////////////////
// Param
///////////////////////////////////////////////////////////////////////////

/**
 * Base for output parameter.
 */
public abstract class Sqlite3ParamBase<Value, Var : CPointed>
internal constructor(initialValue: Value) : Sqlite3Param<Value> {

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
public abstract class Sqlite3PointerParamBase<Value, Var : CPointed> :
    Sqlite3ParamBase<Value?, CPointerVar<Var>>(null) {

    final override fun NativePlacement.allocate(initialValue: Value?): CPointerVar<Var> {
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


public actual class Sqlite3IntParam actual constructor(initialValue: Int) :
    Sqlite3ParamBase<Int, IntVar>(initialValue) {

    override fun NativePlacement.allocate(initialValue: Int): IntVar {
        return alloc(value)
    }

    override fun readValue(pointer: CPointer<IntVar>): Int {
        return pointer.pointed.value
    }
}

public actual class Sqlite3LongParam actual constructor(initialValue: Long) :
    Sqlite3ParamBase<Long, LongVar>(initialValue) {

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

public actual class Sqlite3StringUtf8Param actual constructor() :
    Sqlite3PointerParamBase<String?, ByteVar>() {

    override fun create(pointer: CPointer<ByteVar>): String {
        return pointer.toKStringFromUtf8()
    }
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3DatabaseConnectionParam actual constructor() :
    Sqlite3PointerParamBase<sqlite3, s3>() {

    override fun create(pointer: CPointer<s3>): sqlite3 {
        return sqlite3(pointer)
    }
}

public actual class Sqlite3BlobParam actual constructor() :
    Sqlite3PointerParamBase<sqlite3_blob, s3_blob>() {

    override fun create(pointer: CPointer<s3_blob>): sqlite3_blob {
        return sqlite3_blob(pointer)
    }
}

public actual class Sqlite3ContextParam actual constructor() :
    Sqlite3PointerParamBase<sqlite3_context, s3_context>() {

    override fun create(pointer: CPointer<s3_context>): sqlite3_context {
        return sqlite3_context(pointer)
    }
}

public actual class Sqlite3StatementParam actual constructor() :
    Sqlite3PointerParamBase<sqlite3_stmt, s3_stmt>() {

    override fun create(pointer: CPointer<s3_stmt>): sqlite3_stmt {
        return sqlite3_stmt(pointer)
    }
}

public actual class Sqlite3ValueParam actual constructor() :
    Sqlite3PointerParamBase<sqlite3_value, s3_value>() {

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
internal inline fun <Var : CPointed, R> Sqlite3ParamBase<*, Var>.use(
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
 * Allocates [Var] into a [kotlinx.cinterop.MemScope], invokes [block] with a pointer to it and
 * returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <Var : CPointed, R> Sqlite3ParamBase<*, Var>.useMemScoped(
    block: (CPointer<Var>) -> R
): R = memScoped {
    use(this, block)
}