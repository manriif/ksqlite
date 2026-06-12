package ksqlite.kapi.impl.helpers

/**
 * Helper for objects implementing [AutoCloseable].
 */
@PublishedApi
internal open class ClosableScope : AutoCloseable {

    @PublishedApi
    internal var closed = false
        private set

    /**
     * Returns [block]'s result or throws [IllegalStateException] if [closed] is `true`.
     */
    @PublishedApi
    internal inline fun <R> notClosed(
        lazyMessage: () -> String = { "Scope is closed" },
        block: () -> R
    ): R {
        check(!closed, lazyMessage)
        return block()
    }

    override fun close() {
        closed = true
    }
}