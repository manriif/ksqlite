package ksqlite.capi.memory

import ksqlite.capi.interop.wasm.Sqlite3Wasm
import ksqlite.capi.interop.sqlite3
import ksqlite.capi.interop.wasm.WasmPointer

public actual open class GenericPointer internal constructor(internal val pointer: WasmPointer)

///////////////////////////////////////////////////////////////////////////
// Allocation
///////////////////////////////////////////////////////////////////////////

internal interface MemoryAllocator: AutoCloseable {

}

/**
 *
 */
internal class HeapAllocator : MemoryAllocator {

    private val allocatedPointers = mutableListOf<WasmPointer>()
    private var closed = false

    private inline fun createPointer(size: Int, init: Sqlite3Wasm.() -> WasmPointer): WasmPointer {
       TODO()
    }

    fun allocatePointer(initialAddress: WasmPointer? = null) = createPointer {
        TODO()
    }

    fun allocateUtf8(initialValue: String?): WasmPointer = createPointer {
        TODO()
    }

    fun allocateLong(initialValue: Long): WasmPointer = createPointer {
        TODO()
    }

    fun allocateInt(initialValue: Int): WasmPointer = createPointer {
        TODO()
    }

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