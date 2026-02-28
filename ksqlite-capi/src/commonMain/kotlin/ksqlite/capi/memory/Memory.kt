package ksqlite.capi.memory

import ksqlite.capi.convertResult
import ksqlite.capi.types.Sqlite3Result

/**
 * Pointer pointing to anything.
 */
internal expect class GenericPointer

///////////////////////////////////////////////////////////////////////////
// Disposables
///////////////////////////////////////////////////////////////////////////

/**
 * Clears all the resources owned by ksqlite.
 * It is recommended that all sqlite databases connection are closed calling that function.
 */
public fun ksqliteCleanup() {
    ScopedMemoryManagers
        .onEach { it.key.memory.close() }
        .clear()

    GlobalMemoryManager.clear()
}

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/**
 * Memory manager for top level objects.
 */
private val GlobalMemoryManager: MemoryManager by lazy(::MemoryManager)

/**
 * Returns the global [MemoryManager] instance.
 */
internal val globalMemory: MemoryManager
    get() = GlobalMemoryManager

///////////////////////////////////////////////////////////////////////////
// Scoped
///////////////////////////////////////////////////////////////////////////

/**
 * Per pointer memory manager.
 */
private val ScopedMemoryManagers = mutableMapOf<GenericPointer, MemoryManager>()

/**
 * Returns the [MemoryManager] for `this` [GenericPointer], creating one if necessary.
 */
internal val GenericPointer.memory: MemoryManager
    get() = ScopedMemoryManagers.getOrPut(this, ::MemoryManager)

/**
 * Releases the [MemoryManager] associated with `this` [GenericPointer].
 */
internal fun GenericPointer.destroyMemory() {
    ScopedMemoryManagers.remove(this)?.close()
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes and returns [block]'s result with a [MemoryManager] receiver that is closed before
 * function returns.
 */
internal inline fun <R> useMemoryManager(block: MemoryManager.() -> R): R {
    return MemoryManager().use { manager ->
        block(manager)
    }
}

/**
 * Invokes [block] which is expected to be the SQLite function that will deallocate [Pointer] and
 * returns [block]'s result.
 *
 * If the deallocation succeeds, which is the case if [block] returns [Sqlite3Result.OK], then
 * all the resources associated with [Pointer] through [memory] are disposed and [memory] is
 * closed before the function returns.
 */
internal inline fun <Pointer : GenericPointer> Pointer.deallocate(
    block: (Pointer) -> Int
): Sqlite3Result {
    val result = convertResult(block(this))

    if (result == Sqlite3Result.OK) {
        destroyMemory()
    }

    return result
}

/**
 * Invokes [block] which is expected to be the SQLite function that will deallocate [Pointer] and
 * returns [block]'s result.
 *
 * If the deallocation succeeds, which is the case if [block] returns [Sqlite3Result.OK], then
 * all the resources associated with [Pointer] through [memory] are disposed and [memory] is
 * closed before the function returns.
 */
internal inline fun <Pointer : GenericPointer> Pointer?.deallocateNullable(
    block: (Pointer?) -> Int
): Sqlite3Result {
    if (this == null) {
        return convertResult(block(null))
    }

    return this.deallocate(block)
}