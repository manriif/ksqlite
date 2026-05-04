package ksqlite.capi.types

import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.memory.HeapAllocator
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.heapScoped
import ksqlite.capi.utils.isNull

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
    internal abstract fun HeapAllocator.allocate(initialValue: Value): WasmPointer

    /**
     * Reads the current [Value] from [pointer].
     */
    protected abstract fun readValue(pointer: WasmPointer): Value

    /**
     * Allocates memory into [allocator] and returns the [WasmPointer] to the allocated [Value].
     */
    internal fun attach(allocator: HeapAllocator): WasmPointer {
        return allocator.allocate(actualValue)
    }

    /**
     * Extracts the value of the previously allocated [Value] from [pointer].
     */
    internal fun detach(pointer: WasmPointer) {
        actualValue = readValue(pointer)
    }
}

/**
 * Base for pointer output parameter.
 */
public abstract class Sqlite3PointerOutParamBase<Value> : Sqlite3OutParamBase<Value?>(null) {

    final override fun HeapAllocator.allocate(initialValue: Value?): WasmPointer {
        return TODO()//allocatePointer(initialValue)
    }

    /**
     * Creates a new [Value] from non-null pointing [pointer]
     */
    protected abstract fun create(pointer: WasmPointer): Value

    final override fun readValue(pointer: WasmPointer): Value? {
        if (pointer.isNull) {
            return null
        }

        return create(pointer)
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual open class Sqlite3IntOutParam actual constructor(initialValue: Int) :
    Sqlite3OutParamBase<Int>(initialValue) {

    override fun HeapAllocator.allocate(initialValue: Int): WasmPointer {
        return allocateInt(initialValue)
    }

    override fun readValue(pointer: WasmPointer): Int {
        return TODO()
    }
}

public actual class Sqlite3LongOutParam actual constructor(initialValue: Long) :
    Sqlite3OutParamBase<Long>(initialValue) {

    override fun HeapAllocator.allocate(initialValue: Long): WasmPointer {
        return allocateLong(initialValue)
    }

    override fun readValue(pointer: WasmPointer): Long {
        return TODO()
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

    override fun create(pointer: WasmPointer): String {
        TODO()
        /*val size = size ?: return pointer.getStringUtf8()

        return pointer
            .asSlice(0, size.toLong())
            .getStringUtf8()*/
    }
}

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3DatabaseConnectionOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3>() {

    override fun create(pointer: WasmPointer): sqlite3 {
        return sqlite3(pointer)
    }
}

public actual class Sqlite3ContextOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_context>() {

    override fun create(pointer: WasmPointer): sqlite3_context {
        return sqlite3_context(pointer)
    }
}

public actual class Sqlite3StatementOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_stmt>() {

    override fun create(pointer: WasmPointer): sqlite3_stmt {
        return sqlite3_stmt(pointer)
    }
}

public actual class Sqlite3ValueOutParam actual constructor() :
    Sqlite3PointerOutParamBase<sqlite3_value>() {

    override fun create(pointer: WasmPointer): sqlite3_value {
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
    allocator: HeapAllocator,
    block: (WasmPointer) -> R
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
 * Allocates native memory for [param] into `this` [HeapAllocator], invokes [block] with a
 * pointer to it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> HeapAllocator.useParam(
    param: Sqlite3OutParamBase<*>?,
    block: (WasmPointer) -> R
): R {
    if (param == null) {
        return block(NullPtr)
    }

    return param.use(this, block)
}

/**
 * Allocates native memory for [param]  into a [HeapAllocator], invokes [block] with a pointer to
 * it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> useParamMemScoped(
    param: Sqlite3OutParamBase<*>?,
    block: (WasmPointer) -> R
): R {
    if (param == null) {
        return block(NullPtr)
    }

    return heapScoped {
        param.use(this, block)
    }
}

/**
 * Allocates native memory to [param1] and [param2] into `this` [HeapAllocator], invokes [block]
 * with pointers to them and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <R> HeapAllocator.useParams(
    param1: Sqlite3OutParamBase<*>?,
    param2: Sqlite3OutParamBase<*>?,
    block: (
        pointer1: WasmPointer,
        pointer2: WasmPointer
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(NullPtr, NullPtr)
    }

    val pointer1 = param1?.attach(this) ?: NullPtr
    val pointer2 = param2?.attach(this) ?: NullPtr

    return try {
        block(pointer1, pointer2)
    } finally {
        param1?.detach(pointer1)
        param2?.detach(pointer2)
    }
}

/**
 * Allocates native memory to [param1] and [param2] into a [HeapAllocator], invokes [block] with
 * pointers to them and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <R> useParamsMemScoped(
    param1: Sqlite3OutParamBase<*>?,
    param2: Sqlite3OutParamBase<*>?,
    block: (
        pointer1: WasmPointer,
        pointer2: WasmPointer
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(NullPtr, NullPtr)
    }

    return heapScoped {
        val pointer1 = param1?.attach(this) ?: NullPtr
        val pointer2 = param2?.attach(this) ?: NullPtr

        try {
            block(pointer1, pointer2)
        } finally {
            param1?.detach(pointer1)
            param2?.detach(pointer2)
        }
    }
}