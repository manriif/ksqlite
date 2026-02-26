package ksqlite.capi.memory

/**
 * Platform raw pointer.
 */
internal expect class RawPointer

/**
 * Memory manager for top level objects.
 */
private val GlobalMemoryManager: MemoryManager by lazy(::MemoryManager)
private val KeyedPointers: MutableMap<String, RawPointer> by lazy(::mutableMapOf)

/**
 * Disposes [pointer].
 */
internal expect fun disposeRawPointer(pointer: RawPointer)

/**
 * Clears all the globally stored resources.
 */
public fun ksqliteClearGlobalMemory() {
    KeyedPointers
        .onEach { disposeRawPointer(it.value) }
        .clear()

    GlobalMemoryManager.clear()
}

/**
 * Disposes any resource previously associated with [key] and stores new resource obtained from
 * [block].
 */
internal inline fun <Pointer: RawPointer> globalPointer(
    key: String,
    block: MemoryManager.() -> Pointer?
): Pointer? {
    val pointer = block(GlobalMemoryManager)

    val oldPointer = if (pointer != null) {
        KeyedPointers.put(key, pointer)
    } else {
        KeyedPointers.remove(key)
    }

    if (oldPointer != null) {
        disposeRawPointer(oldPointer)
    }

    return pointer
}