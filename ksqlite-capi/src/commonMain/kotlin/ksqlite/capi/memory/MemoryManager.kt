package ksqlite.capi.memory

/**
 * Manages memory.
 */
internal abstract class MemoryManagerBase : AutoCloseable {

    private var closed = false

    /**
     * Releases all the resources but keep the manager alive.
     */
    abstract fun clear()

    /**
     * Invokes and returns [block]'s result throwing an [IllegalStateException] if this instance is
     * closed.
     */
    protected inline fun <T> notClosed(block: () -> T): T {
        check(!closed) { "Manager is closed" }
        return block()
    }

    final override fun close() {
        if (!closed) {
            clear()
            closed = true
        }
    }
}

/**
 * Platform specific memory manager.
 */
internal expect class MemoryManager() : MemoryManagerBase {
    override fun clear()
}