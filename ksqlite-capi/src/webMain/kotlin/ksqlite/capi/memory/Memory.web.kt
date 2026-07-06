@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package ksqlite.capi.memory

import js.typedarrays.Int8Array
import ksqlite.capi.wasm
import ksqlite.foreign.js.arrayForEachIndexed
import ksqlite.foreign.js.arraySize
import ksqlite.foreign.js.asInt8Array
import ksqlite.foreign.js.plus
import ksqlite.foreign.js.toByteArray
import ksqlite.foreign.js.toInt8Array
import ksqlite.foreign.wasm.CString
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.IR
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmMemory
import ksqlite.foreign.wasm.WasmPStack
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.alloc
import ksqlite.foreign.wasm.allocPtr
import ksqlite.foreign.wasm.installFunction
import ksqlite.foreign.wasm.scopedAllocCStringStruct
import ksqlite.foreign.wasm.scopedAllocPtr
import ksqlite.foreign.wasm.sizeofIR
import kotlin.js.JsAny
import kotlin.js.JsReference
import kotlin.js.get
import kotlin.js.toJsBigInt
import kotlin.js.toJsReference
import kotlin.js.toLong

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Wasm null pointer.
 */
public val NullPtr: WasmPointer
    get() = wasm.ptr.`null`

/**
 * Whether `this` [WasmPointer] points to a null pointer.
 */
internal val WasmPointer.isNull: Boolean
    inline get() = this == NullPtr

/**
 * Returns `null` if `this` [WasmPointer] points to a null pointer.
 */
internal val WasmPointer.orNull: WasmPointer?
    inline get() = takeUnless { isNull }

/**
 * Returns a non-null [WasmPointer].
 */
internal val WasmPointer?.notNull: WasmPointer
    inline get() = this ?: NullPtr

/**
 * Sets the pointer value of `this` pointer to pointer.
 */
internal fun WasmPointer.setPointerValue(
    value: WasmPointer,
    memory: WasmMemory = wasm
) {
    memory.pokePtr(this, value)
}

/**
 * Sets the [Long] value of `this` pointer to long.
 */
internal fun WasmPointer.setValue(
    value: Long,
    memory: WasmMemory = wasm
) {
    memory.poke64(this, value.toJsBigInt())
}

/**
 * Sets the [Int] value of `this` pointer to int.
 */
internal fun WasmPointer.setValue(
    value: Int,
    memory: WasmMemory = wasm
) {
    memory.poke32(this, value)
}

/**
 * Returns [Pointer] instantiated after [factory] which is passed `this` non-null pointing [Long].
 */
internal fun <Pointer : Struct> WasmPointer.wrapOrNull(factory: (WasmPointer) -> Pointer): Pointer? =
    orNull?.let(factory)

///////////////////////////////////////////////////////////////////////////
// Allocators
///////////////////////////////////////////////////////////////////////////

/**
 * Wasm memory allocator.
 * The allocated memory is zeroed depending on the implementation.
 */
internal interface MemoryAllocator : AutoCloseable {

    /**
     * The associated [WasmMemory].
     */
    val memory: WasmMemory

    /**
     * Allocates [byteCount] and returns a pointer to the first byte.
     */
    fun allocate(byteCount: Int): WasmPointer

    /**
     * Allocates [byteCount] and returns a pointer to the first byte.
     */
    fun allocate(byteCount: IR): WasmPointer

    /**
     * Allocates a pointer to pointer and returns the pointer address.
     */
    fun allocatePointer(): WasmPointer
}

/**
 * Base for [MemoryAllocator] implementors.
 */
internal abstract class AllocatorScope(override val memory: WasmMemory) : MemoryAllocator {

    private var closed: Boolean = false

    protected inline fun <R> notClosed(block: () -> R): R {
        check(!closed) { "Allocator is closed" }
        return block()
    }

    protected abstract fun cleanup()

    final override fun close() {
        if (!closed) {
            closed = true
            cleanup()
        }
    }
}

/**
 * Memory allocator allocating in [scope] attached to [memory].
 */
internal class HeapAllocatorScope(
    memory: WasmMemory,
    private val scope: JsAny
) : AllocatorScope(memory) {

    override fun allocate(byteCount: Int): WasmPointer = notClosed {
        memory.scopedAlloc(byteCount)
    }

    override fun allocate(byteCount: IR): WasmPointer = notClosed {
        memory.scopedAlloc(memory.sizeofIR(byteCount))
    }

    override fun allocatePointer(): WasmPointer = notClosed {
        memory.scopedAllocPtr()
    }

    /**
     * Allocates a pointer and initializes it with [utf8]'s content + a NUL terminator.
     */
    fun allocateUtf8(utf8: String): CString = notClosed {
        memory.scopedAllocCStringStruct(utf8)
    }

    override fun cleanup() {
        memory.scopedAllocPop(scope)
    }
}

/**
 * Allocates a pointer, initializes it with `this` UTF-8 string's content + a NUL terminator.
 */
context(scope: HeapAllocatorScope)
internal fun String.allocateUtf8(): CString = scope.allocateUtf8(this)

/**
 * Allocates a pointer, initializes it with `this` UTF-8 string's content + a NUL terminator and
 * returns the pointer to it.
 *
 * Returns [NullPtr] if `this` is `null`.
 */
context(scope: HeapAllocatorScope)
internal fun String?.allocateUtf8Pointer(): WasmPointer =
    if (this == null) NullPtr else scope.allocateUtf8(this).pointer

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 */
context(scope: HeapAllocatorScope)
internal fun allocateUtf8Array(array: Array<String>?): WasmPointer {
    if (array == null) {
        return NullPtr
    }

    val pointerSize = scope.memory.sizeofIR(IR.Ptr)
    val baseArrayPointer = scope.allocate(pointerSize * arraySize(array))

    arrayForEachIndexed(array) { index, string ->
        scope.memory.pokePtr(
            address = baseArrayPointer + (index * pointerSize),
            value = string.allocateUtf8Pointer()
        )
    }

    return baseArrayPointer
}

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> heapScoped(
    memory: WasmMemory = wasm,
    block: HeapAllocatorScope.() -> T
): T = HeapAllocatorScope(memory, memory.scopedAllocPush()).use(block)

/**
 * Memory allocator allocating from the wasm [stack].
 */
internal class StackAllocatorScope(
    memory: WasmMemory,
    private val stack: WasmPStack,
    private val stackPointer: JsAny
) : AllocatorScope(memory) {

    override fun allocate(byteCount: Int): WasmPointer = notClosed {
        stack.alloc(byteCount)
    }

    override fun allocate(byteCount: IR): WasmPointer = notClosed {
        stack.alloc(byteCount)
    }

    override fun allocatePointer(): WasmPointer = notClosed {
        stack.allocPtr()
    }

    override fun cleanup() {
        stack.restore(stackPointer)
    }
}

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> stackScoped(
    memory: WasmMemory = wasm,
    block: StackAllocatorScope.() -> T
): T {
    val stack = wasm.pstack
    return StackAllocatorScope(memory, stack, stack.pointer).use(block)
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * Function accepting a pointer.
 */
internal fun interface ReferenceFunction {

    /**
     * Handles the [refPointer].
     */
    fun apply(refPointer: WasmPointer)
}

@JsFun("(jsRef, handler) => (p0) => handler(jsRef, p0)")
private external fun refFunction(
    jsRef: JsReference<ReferenceFunction>,
    handler: (
        jsRef: JsReference<ReferenceFunction>,
        refPointer: WasmPointer
    ) -> Unit
): JsFunction

/**
 * Allocates a new upcall stub, that invokes [ReferenceFunction.apply] on [function].
 */
internal fun WasmFunctions.installReferenceFunction(function: ReferenceFunction): WasmPointer =
    installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = refFunction(function.toJsReference()) { jsRef, refPointer ->
            jsRef.get().apply(refPointer)
        }
    )

/**
 * Runs and returns [block]'s result, passing it the [JsFunction] associated with this
 * [WasmPointer].
 */
internal fun <R> WasmPointer.usingJsFunction(
    functions: WasmFunctions = wasm,
    block: (function: JsFunction) -> R
): R {
    check(!isNull) {
        "Can't obtain a JsFunction from a null pointer"
    }

    val function = checkNotNull(functions.functionEntry(this)) {
        "Failed to obtain a JsFunction from address $this"
    }

    return block(function)
}

///////////////////////////////////////////////////////////////////////////
// Arrays
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an array of [count] item of type [T] obtained from [transform].
 */
internal inline fun <reified T> WasmPointer.toArray(
    count: Int,
    memory: WasmMemory = wasm,
    transform: WasmMemory.(WasmPointer) -> T
): Array<T> {
    if (count == 0) {
        return emptyArray()
    }

    val ptrSize = memory.sizeofIR(IR.Ptr)

    return Array(count) { index ->
        transform(memory, memory.peekPtr(plus(ptrSize * index)))
    }
}

/**
 * Returns an array of [count] item of type [T] obtained from [transform].
 * Returns an empty array if `this` is `null`.
 */
internal inline fun <reified T> WasmPointer.toArrayOrEmpty(
    count: Int,
    memory: WasmMemory = wasm,
    transform: WasmMemory.(WasmPointer) -> T
): Array<T> = orNull?.toArray(count, memory, transform) ?: emptyArray()

/**
 * Reads and returns an array of [count] String.
 */
internal fun WasmPointer.toNullableStringArray(
    count: Int,
    memory: WasmMemory = wasm
): Array<String?> = toArray(count, memory) { it.toKStringFromUtf8OrNull() }

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun WasmPointer.toNullableStringArrayOrEmpty(
    count: Int,
    memory: WasmMemory = wasm
): Array<String?> = orNull?.toNullableStringArray(count, memory) ?: emptyArray()

/**
 * Reads and returns an array of [count] String.
 */
internal fun WasmPointer.toStringArray(
    count: Int,
    memory: WasmMemory = wasm
): Array<String> = this.toArray(count, memory) { it.toKStringFromUtf8() }

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun WasmPointer.toStringArrayOrEmpty(
    count: Int,
    memory: WasmMemory = wasm
): Array<String> = orNull?.toStringArray(count, memory) ?: emptyArray()

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates a pointer to store [buffer]'s content to wasm memory then invokes [block] with the
 * pointer. The allocated memory is reclaimed after [block] returns or throws.
 */
internal inline fun <R> bufferScoped(
    buffer: ByteArray,
    memory: WasmMemory = wasm,
    size: Int? = null,
    block: Int8Array<*>.(buffer: WasmPointer) -> R
): R {
    val typedArray = size?.let { toInt8Array(buffer, it) } ?: asInt8Array(buffer)
    val pointer = memory.allocFromTypedArray(typedArray)

    return try {
        block(typedArray, pointer)
    } finally {
        memory.dealloc(pointer)
    }
}

/**
 * Reads [size] bytes starting from this pointer and returns them as [ByteArray].
 */
internal fun WasmPointer.readByteArray(
    size: Int,
    memory: WasmMemory = wasm
): ByteArray {
    val begin = toLong().toInt()
    val end = begin + size
    val region = memory.heap8().subarray(begin, end)

    return toByteArray(region)
}

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
internal fun WasmPointer.toKStringFromUtf8OrNull(memory: WasmMemory = wasm): String? =
    memory.cstrToJs(this)?.toString()

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
context(memory: WasmMemory)
internal fun WasmPointer.toKStringFromUtf8OrNull(): String? = toKStringFromUtf8OrNull(memory)

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
internal fun WasmPointer.toKStringFromUtf8(memory: WasmMemory = wasm): String =
    checkNotNull(toKStringFromUtf8OrNull(memory))

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
context(memory: WasmMemory)
internal fun WasmPointer.toKStringFromUtf8(): String = toKStringFromUtf8(memory)

/**
 * Reads [size] bytes from this pointer and then convert to string.
 */
internal fun WasmPointer.toKStringFromUtf8(
    size: Int,
    memory: WasmMemory = wasm
): String = memory
    .typedArrayToString(memory.heap8u(), this, plus(size))
    .toString()

/**
 * Reads [size] bytes from this pointer and then convert to string.
 */
context(memory: WasmMemory)
internal fun WasmPointer.toKStringFromUtf8(size: Int): String = toKStringFromUtf8(size, memory)