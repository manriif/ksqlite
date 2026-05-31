package ksqlite.capi.memory

import ksqlite.capi.convertResult
import ksqlite.capi.types.Sqlite3Result

///////////////////////////////////////////////////////////////////////////
// Struct
///////////////////////////////////////////////////////////////////////////

/**
 * Base for [StructPointer].
 */
public abstract class StructPointerBase internal constructor() {

    internal abstract val address: Long

    override fun toString(): String {
        return "${this::class.simpleName}(address=0x${address.toHexString()})"
    }
}

/**
 * Pointer to an sqlite3 struct.
 */
public expect open class StructPointer: StructPointerBase {
    override val address: Long
}

/**
 * Invokes [block] which is expected to be the SQLite function that will deallocate [Scope] and
 * returns [block]'s result.
 *
 * If the deallocation succeeds, which is the case if [block] returns [Sqlite3Result.OK], then
 * all the resources associated with [Scope] through [memory] are disposed and [memory] is
 * closed before the function returns.
 */
internal inline fun <Scope : MemoryScope> Scope.deallocate(
    block: (Scope) -> Int
): Sqlite3Result {
    val result = convertResult(block(this))

    if (result == Sqlite3Result.OK) {
        destroyMemory()
    }

    return result
}

/**
 * Invokes [block] which is expected to be the SQLite function that will deallocate [Scope] and
 * returns [block]'s result.
 *
 * If the deallocation succeeds, which is the case if [block] returns [Sqlite3Result.OK], then
 * all the resources associated with [Scope] through [memory] are disposed and [memory] is
 * closed before the function returns.
 */
internal inline fun <Scope : MemoryScope> Scope?.deallocateNullable(
    block: (Scope?) -> Int
): Sqlite3Result {
    if (this == null) {
        return convertResult(block(null))
    }

    return this.deallocate(block)
}

///////////////////////////////////////////////////////////////////////////
// Concurrency
///////////////////////////////////////////////////////////////////////////

internal typealias ConcurrentMap<K, V> = co.touchlab.stately.collections.ConcurrentMutableMap<K, V>
internal typealias Lock = co.touchlab.stately.concurrency.Lock

///////////////////////////////////////////////////////////////////////////
// Managers
///////////////////////////////////////////////////////////////////////////

/**
 * Marker for object having a clearly defined lifecycle and that can be associated with a
 * [MemoryManager].
 */
public interface MemoryScope

/**
 * Memory manager for top level objects.
 */
private val GlobalMemoryManager: MemoryManager by lazy(::MemoryManager)

/**
 * Returns the global [MemoryManager] instance.
 */
internal val globalMemory: MemoryManager
    get() = GlobalMemoryManager

/**
 * Per pointer memory manager.
 */
private val ScopedMemoryManagers = ConcurrentMap<MemoryScope, MemoryManager>()

/**
 * Returns the [MemoryManager] for `this` [StructPointer], creating one if necessary.
 */
internal val MemoryScope.memory: MemoryManager
    get() = ScopedMemoryManagers.computeIfAbsent(this) { MemoryManager() }

/**
 * Returns the [MemoryManager] for `this` [StructPointer] or `null`.
 */
internal val MemoryScope.memoryOrNull: MemoryManager?
    get() = ScopedMemoryManagers[this]

/**
 * Invokes [block] with the [MemoryManager] associated to this pointer as receiver.
 */
internal inline fun <R> MemoryScope.withMemoryManager(block: MemoryManager.() -> R): R {
    return memory.block()
}

/**
 * Releases the [MemoryManager] associated with `this` [StructPointer].
 */
internal fun MemoryScope.destroyMemory() {
    ScopedMemoryManagers.remove(this)?.close()
}

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
 * Clears all the resources owned by ksqlite.
 * It is recommended to close all opened sqlite database connections before calling that function.
 */
public fun ksqliteCleanup() {
    val keys = ScopedMemoryManagers.keys

    for (key in keys) {
        ScopedMemoryManagers
            .remove(key)
            ?.close()
    }

    GlobalMemoryManager.clear()
}