package ksqlite.capi.memory

import ksqlite.capi.exports
import ksqlite.foreign.wasm.IR
import ksqlite.foreign.wasm.WasmMemory
import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.toInt
import kotlin.js.toJsBigInt
import kotlin.js.toLong

/**
 * Base for output parameter.
 */
public abstract class OutputParamBase<Value> internal constructor(initialValue: Value) :
    OutputParam<Value> {

    private var actualValue: Value = initialValue
    private var attachedMemory: WasmMemory? = null

    final override val value: Value
        get() = actualValue

    /**
     * Allocates memory and initializes with [initialValue].
     */
    internal abstract fun MemoryAllocator.allocate(initialValue: Value): WasmPointer

    /**
     * Reads the current [Value] from [pointer].
     */
    internal abstract fun WasmMemory.readValue(pointer: WasmPointer): Value

    /**
     * Allocates memory into [allocator] and returns the [WasmPointer] to the allocated [Value].
     */
    internal fun attach(allocator: MemoryAllocator): WasmPointer {
        check(attachedMemory == null) { "A memory is already attached to the parameter" }
        attachedMemory = allocator.memory
        return allocator.allocate(actualValue)
    }

    /**
     * Extracts the value of the previously allocated [Value] from [pointer].
     */
    internal fun detach(pointer: WasmPointer) {
        val memory = checkNotNull(attachedMemory) { "No memory is attached to the parameter" }
        actualValue = memory.readValue(pointer)
    }
}

/**
 * Base for pointer output parameter.
 */
public abstract class PointerOutputParam<Value> : OutputParamBase<Value?>(null) {

    final override fun MemoryAllocator.allocate(initialValue: Value?): WasmPointer {
        ensurePointerInitialValueIsNull(initialValue)
        return allocatePointer()
    }

    /**
     * Creates a new [Value] from non-null pointing [pointer].
     */
    internal abstract fun WasmMemory.create(pointer: WasmPointer): Value

    final override fun WasmMemory.readValue(pointer: WasmPointer): Value? {
        return peekPtr(pointer).orNull?.let { create(it) }
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual open class Int32OutputParam actual constructor(initialValue: Int) :
    OutputParamBase<Int>(initialValue) {

    override fun MemoryAllocator.allocate(initialValue: Int): WasmPointer {
        val address = allocate(IR.I32)
        memory.poke32(address, initialValue)
        return address
    }

    override fun WasmMemory.readValue(pointer: WasmPointer): Int {
        return peek32(pointer).toInt()
    }
}

public actual class Int64OutputParam actual constructor(initialValue: Long) :
    OutputParamBase<Long>(initialValue) {

    override fun MemoryAllocator.allocate(initialValue: Long): WasmPointer {
        val address = allocate(IR.I64)
        memory.poke64(address, initialValue.toJsBigInt())
        return address
    }

    override fun WasmMemory.readValue(pointer: WasmPointer): Long {
        return peek64(pointer).toLong()
    }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

public actual class Utf8OutputParam actual constructor() : PointerOutputParam<String>() {

    private var freeOnRead: Boolean = false
    private var customSize: Int32OutputParam? = null

    override fun WasmMemory.create(pointer: WasmPointer): String {
        val string = customSize?.value
            ?.let { pointer.toKStringFromUtf8(it) }
            ?: pointer.toKStringFromUtf8()

        if (freeOnRead) {
            exports.sqlite3_free(pointer)
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
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates value into [allocator], invokes [block] with a pointer to it and returns [block]'s
 * result.
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> OutputParamBase<*>.use(
    allocator: MemoryAllocator,
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
 * Allocates native memory for [param] into `this` [MemoryAllocator], invokes [block] with a
 * pointer to it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> MemoryAllocator.useParam(
    param: OutputParamBase<*>?,
    block: (WasmPointer) -> R
): R {
    if (param == null) {
        return block(NullPtr)
    }

    return param.use(this, block)
}

/**
 * Allocates native memory for [param]  into a [StackAllocatorScope], invokes [block] with a pointer
 * to it and returns [block]'s result.
 *
 * The pointer passed to [block] must not escape.
 */
internal inline fun <R> useParamStackScoped(
    param: OutputParamBase<*>?,
    block: (WasmPointer) -> R
): R {
    if (param == null) {
        return block(NullPtr)
    }

    return stackScoped {
        param.use(this, block)
    }
}

/**
 * Allocates native memory to [param1] and [param2] into `this` [MemoryAllocator], invokes [block]
 * with pointers to them and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <R> MemoryAllocator.useParams(
    param1: OutputParamBase<*>?,
    param2: OutputParamBase<*>?,
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
 * Allocates native memory to [param1] and [param2] into a [StackAllocatorScope], invokes [block]
 * with pointers to them and returns [block]'s result.
 *
 * The pointers passed to [block] must not escape.
 */
internal inline fun <R> useParamsStackScoped(
    param1: OutputParamBase<*>?,
    param2: OutputParamBase<*>?,
    block: (
        pointer1: WasmPointer,
        pointer2: WasmPointer
    ) -> R
): R {
    if (param1 == null && param2 == null) {
        return block(NullPtr, NullPtr)
    }

    return stackScoped {
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