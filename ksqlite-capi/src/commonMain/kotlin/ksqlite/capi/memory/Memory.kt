package ksqlite.capi.memory

///////////////////////////////////////////////////////////////////////////
// Concurrency
///////////////////////////////////////////////////////////////////////////

internal typealias ConcurrentMap<K, V> = co.touchlab.stately.collections.ConcurrentMutableMap<K, V>
internal typealias Lock = co.touchlab.stately.concurrency.Lock

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
public expect open class StructPointer : StructPointerBase {
    override val address: Long
}

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
private val GlobalMemoryManager by lazy(::MemoryManager)

/**
 * Returns the global [MemoryManager] instance.
 */
internal val globalMemory: MemoryManager
    get() = GlobalMemoryManager

/**
 * Per pointer memory manager.
 */
private val ScopedMemoryManagers by lazy { ConcurrentMap<Long, MemoryManager>() }

/**
 * Returns the [MemoryManager] for `this` [StructPointer], creating one if necessary.
 */
internal val <S> S.memory: MemoryManager where S : StructPointer, S : MemoryScope
    get() = ScopedMemoryManagers.computeIfAbsent(address) { MemoryManager() }

/**
 * Returns the [MemoryManager] for `this` [StructPointer] or `null`.
 */
internal val <S> S.memoryOrNull: MemoryManager? where S : StructPointer, S : MemoryScope
    get() = ScopedMemoryManagers[address]

/**
 * Invokes [block] with the [MemoryManager] associated to this pointer as receiver.
 */
internal inline fun <S, R> S.withMemoryManager(block: MemoryManager.() -> R): R
        where S : StructPointer, S : MemoryScope = memory.block()

/**
 * Releases the [MemoryManager] associated with `this` [StructPointer].
 */
internal fun <S> S.destroyMemory() where S : StructPointer, S : MemoryScope {
    ScopedMemoryManagers.remove(address)?.close()
}

/**
 * Invokes and returns [block]'s result with a [MemoryManager] receiver that is closed before
 * function returns.
 */
internal inline fun <R> useMemoryManager(block: MemoryManager.() -> R): R =
    MemoryManager().use { block(it) }

/**
 * Clears all the resources owned by ksqlite.
 * It is recommended to close all opened sqlite database connections before calling that function.
 *
 * If all the resources have been correctly cleaned up before that method call, `true` is returned.
 * If `false` is returned then either some resource(s) have not been cleaned up correctly or something
 * escaped its owner or both.
 */
public fun ksqliteCleanup(): Boolean {
    val keys = ScopedMemoryManagers.keys

    for (key in keys) {
        ScopedMemoryManagers.remove(key)?.close()
    }

    // May contains top level callbacks registered with sqlite3_config() (log/sqllog)
    GlobalMemoryManager.clear()

    return keys.isEmpty()
}