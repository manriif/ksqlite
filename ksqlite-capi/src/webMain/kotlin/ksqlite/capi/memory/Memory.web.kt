@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package ksqlite.capi.memory

import ksqlite.capi.interop.js.plus
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

public actual open class GenericPointer internal constructor(internal val pointer: WasmPointer)

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
 * Allocates a pointer, initializes it with `this` UTF-8 string's content + a NUL terminator and
 * returns the pointer to it.
 */
context(scope: HeapAllocatorScope)
internal fun String.allocateUtf8(): WasmPointer {
    return scope.allocateUtf8(this).pointer
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
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Reads bytes from this pointer until NULL and then convert to string.
 */
internal fun WasmPointer.toKStringFromUtf8(memory: WasmMemory = wasm): String {
    return memory
        .cstrToJs(this)
        .toString()
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