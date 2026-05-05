package ksqlite.capi.memory

import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.Sqlite3Wasm
import ksqlite.capi.interop.wasm.WasmPointer

public actual open class GenericPointer internal constructor(internal val pointer: WasmPointer)

///////////////////////////////////////////////////////////////////////////
// Allocation
///////////////////////////////////////////////////////////////////////////

internal interface MemoryAllocator : AutoCloseable {

}

private inline fun MemoryAllocator.createPointer(
    /*size: Int, */
    init: Sqlite3Wasm.() -> WasmPointer
): WasmPointer {
    TODO()
}

internal fun MemoryAllocator.allocatePointer(initialValue: WasmPointer = NullPtr) = createPointer {
    TODO()
}

internal fun MemoryAllocator.allocateUtf8(initialValue: String? = null): WasmPointer = createPointer {
    TODO()
}

internal fun MemoryAllocator.allocateLong(initialValue: Long = 0L): WasmPointer = createPointer {
    TODO()
}

internal fun MemoryAllocator.allocateInt(initialValue: Int = 0): WasmPointer = createPointer {
    TODO()
}

/**
 *
 */
internal class HeapAllocator : MemoryAllocator {

    private val allocatedPointers = mutableListOf<WasmPointer>()
    private var closed = false

    override fun close() {
        check(!closed) { "Allocator is closed" }
        closed = true

        allocatedPointers
            .onEach { TODO() }
            .clear()
    }
}

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> memScoped(block: HeapAllocator.() -> T): T {
    return HeapAllocator().use(block)
}

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> heapScoped(block: HeapAllocator.() -> T): T {
    return HeapAllocator().use(block)
}

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> stackScoped(block: HeapAllocator.() -> T): T {
    return HeapAllocator().use(block)
}