@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package ksqlite.capi.memory

import ksqlite.capi.interop.js.Int8Array
import ksqlite.capi.interop.js.arrayForEachIndexed
import ksqlite.capi.interop.js.arraySize
import ksqlite.capi.interop.js.plus
import ksqlite.capi.interop.js.toByteArray
import ksqlite.capi.interop.js.toInt8Array
import ksqlite.capi.interop.wasm.CString
import ksqlite.capi.interop.wasm.IR
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmMemory
import ksqlite.capi.interop.wasm.WasmPStack
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.alloc
import ksqlite.capi.interop.wasm.allocPtr
import ksqlite.capi.interop.wasm.scopedAllocCStringStruct
import ksqlite.capi.interop.wasm.scopedAllocPtr
import ksqlite.capi.interop.wasm.sizeofIR
import ksqlite.capi.wasm
import kotlin.js.JsAny
import kotlin.js.toLong

public actual open class StructPointer internal constructor(internal val pointer: WasmPointer) :
    StructPointerBase() {

    actual override val address: Long
        get() = pointer.toLong()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructPointer) return false

        return pointer == other.pointer

    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }
}

/**
 * Memory manager that is never cleared.
 */
internal val StaticMemoryManager = MemoryManager()

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
internal fun String.allocateUtf8(): CString {
    return scope.allocateUtf8(this)
}

/**
 * Allocates a pointer, initializes it with `this` UTF-8 string's content + a NUL terminator and
 * returns the pointer to it.
 *
 * Returns [NullPtr] if `this` is `null`.
 */
context(scope: HeapAllocatorScope)
internal fun String?.allocateUtf8Pointer(): WasmPointer {
    return if (this == null) NullPtr else scope.allocateUtf8(this).pointer
}

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
): T {
    return HeapAllocatorScope(memory, memory.scopedAllocPush()).use(block)
}

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
// Null
///////////////////////////////////////////////////////////////////////////

/**
 * Whether `this` [WasmPointer] points to a null pointer.
 */
internal val WasmPointer.isNull: Boolean
    get() = this == NullPtr

/**
 * Returns `null` if `this` [WasmPointer] points to a null pointer.
 */
internal val WasmPointer.orNull: WasmPointer?
    get() = takeUnless { isNull }

/**
 * Returns a non-null [WasmPointer].
 */
internal val WasmPointer?.notNull: WasmPointer
    get() = this ?: NullPtr

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
        transform(memory, plus(ptrSize * index))
    }
}

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
    block: Int8Array.(buffer: WasmPointer) -> R
): R {
    val typedArray = size?.let { toInt8Array(buffer, it) } ?: toInt8Array(buffer)
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
internal fun WasmPointer.toKStringFromUtf8OrNull(memory: WasmMemory = wasm): String? {
    return memory
        .cstrToJs(this)
        ?.toString()
}

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
context(memory: WasmMemory)
internal fun WasmPointer.toKStringFromUtf8OrNull(): String? {
    return toKStringFromUtf8OrNull(memory)
}

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
internal fun WasmPointer.toKStringFromUtf8(memory: WasmMemory = wasm): String {
    return checkNotNull(toKStringFromUtf8OrNull(memory))
}

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
context(memory: WasmMemory)
internal fun WasmPointer.toKStringFromUtf8(): String {
    return toKStringFromUtf8(memory)
}

/**
 * Reads [size] bytes from this pointer and then convert to string.
 */
internal fun WasmPointer.toKStringFromUtf8(size: Int, memory: WasmMemory = wasm): String {
    return memory
        .typedArrayToString(memory.heap8u(), this, plus(size))
        .toString()
}

/**
 * Reads [size] bytes from this pointer and then convert to string.
 */
context(memory: WasmMemory)
internal fun WasmPointer.toKStringFromUtf8(size: Int): String {
    return toKStringFromUtf8(size, memory)
}