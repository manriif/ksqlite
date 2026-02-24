package ksqlite.capi.memory

/**
 * Manages memory.
 */
internal expect class MemoryManager() : AutoCloseable {
    override fun close()
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