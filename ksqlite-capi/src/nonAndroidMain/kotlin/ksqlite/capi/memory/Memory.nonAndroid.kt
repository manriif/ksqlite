package ksqlite.capi.memory

/**
 * Memory manager for top level objects.
 */
private val GlobalMemoryManager by lazy(::MemoryManager)

/**
 * Returns the global [MemoryManager] instance.
 */
internal val globalMemory: MemoryManager
    inline get() = GlobalMemoryManager

/**
 * Per pointer memory manager.
 */
private val ScopedMemoryManagers by lazy { ConcurrentMap<Long, MemoryManager>() }

/**
 * Returns the [MemoryManager] for `this` [Struct], creating one if necessary.
 */
internal val <S> S.memory: MemoryManager where S : Struct, S : MemoryScope
    get() = ScopedMemoryManagers.computeIfAbsent(address) { MemoryManager() }

/**
 * Returns the [MemoryManager] for `this` [Struct] or `null`.
 */
internal val <S> S.memoryOrNull: MemoryManager? where S : Struct, S : MemoryScope
    get() = ScopedMemoryManagers[address]

/**
 * Invokes [block] with the [MemoryManager] associated to this pointer as receiver.
 */
internal inline fun <S, R> S.withMemoryManager(block: MemoryManager.() -> R): R
        where S : Struct, S : MemoryScope = memory.block()

/**
 * Releases the [MemoryManager] associated with [S] if any.
 */
internal fun <S> S.destroyMemory() where S : Struct, S : MemoryScope {
    ScopedMemoryManagers.remove(address)?.close()
}

/**
 * Invokes and returns [block]'s result with a [MemoryManager] receiver that is closed before
 * function returns.
 */
internal inline fun <R> useMemoryManager(block: MemoryManager.() -> R): R =
    MemoryManager().use { block(it) }


/**
 * Returns `true` if all the memory managers are empty, `false` otherwise.
 */
internal fun isMemoryEmpty(strict: Boolean = true): Boolean {
    if (!globalMemory.isEmpty) {
        return false
    }

    val keys = ScopedMemoryManagers.keys

    if (keys.isNotEmpty() && strict) {
        return false
    }

    for (key in keys) {
        if (ScopedMemoryManagers[key]?.isEmpty != true) {
            return false
        }
    }

    return true
}