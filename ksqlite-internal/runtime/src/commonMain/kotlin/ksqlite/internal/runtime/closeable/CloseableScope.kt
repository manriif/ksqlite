package ksqlite.internal.runtime.closeable

/**
 * Helper for object implementing [AutoCloseable].
 */
public abstract class CloseableScope : AutoCloseable {

    /**
     * Whether the scope is closed.
     */
    public abstract val closed: Boolean

    /**
     * Notifies about scope closing.
     */
    public open fun onClose(): Unit = Unit

    /**
     * Throws [IllegalStateException] if [closed] is `true`.
     */
    public open fun ensureNotClosed(lazyMessage: () -> String = { "Scope is closed" }) {
        check(!closed, lazyMessage)
    }

    /**
     * Returns [block]'s result or throws [IllegalStateException] if [closed] is `true`.
     */
    public inline fun <R> notClosed(
        crossinline lazyMessage: () -> String = { "Scope is closed" },
        block: () -> R
    ): R {
        ensureNotClosed { lazyMessage() }
        return block()
    }
}