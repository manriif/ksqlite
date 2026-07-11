package ksqlite.kapi.helpers

import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Helper for object implementing [AutoCloseable].
 */
@PublishedApi
internal abstract class ClosableScope : AutoCloseable {

    @PublishedApi
    internal abstract val closed: Boolean

    /**
     * Notifies about scope closing.
     */
    open fun onClose() = Unit

    /**
     * Throws [IllegalStateException] if [closed] is `true`.
     */
    @PublishedApi
    internal open fun ensureNotClosed(lazyMessage: () -> String = { "Scope is closed" }) {
        check(!closed, lazyMessage)
    }

    /**
     * Returns [block]'s result or throws [IllegalStateException] if [closed] is `true`.
     */
    @PublishedApi
    internal inline fun <R> notClosed(
        crossinline lazyMessage: () -> String = { "Scope is closed" },
        block: () -> R
    ): R {
        ensureNotClosed { lazyMessage() }
        return block()
    }
}

/**
 * Unsafe [ClosableScope].
 */
@PublishedApi
internal open class UnsafeClosableScope : ClosableScope() {

    @Volatile
    final override var closed: Boolean = false
        private set

    final override fun close() {
        if (!closed) {
            closed = true
            onClose()
        }
    }
}

/**
 * Subclass of [UnsafeClosableScope] invoking [onClose] when the scope is closed.
 */
internal class DelegatingCloseableScope(
    private val onClose: () -> Unit
) : UnsafeClosableScope() {

    override fun onClose() {
        onClose.invoke()
    }
}

/**
 * Thread-safe [ClosableScope].
 */
@PublishedApi
@OptIn(ExperimentalAtomicApi::class)
internal open class AtomicClosableScope : ClosableScope() {

    private val _closed = AtomicBoolean(false)

    final override val closed: Boolean
        get() = _closed.load()

    final override fun close() {
        if (_closed.compareAndSet(expectedValue = false, newValue = true)) {
            onClose()
        }
    }
}

/**
 * Subclass of [AtomicClosableScope] invoking [onClose] when the scope is closed.
 */
internal class DelegatingAtomicCloseableScope(
    private val onClose: () -> Unit
) : AtomicClosableScope() {

    override fun onClose() {
        onClose.invoke()
    }
}

/**
 * Closable scope which is closed if either [parent] or [child] is closed.
 * The [close] operation is forwarded to [child].
 */
internal open class CombinedClosableScope(
    private val parent: ClosableScope,
    private val child: ClosableScope
) : ClosableScope() {

    final override val closed: Boolean
        get() = child.closed || parent.closed

    override fun ensureNotClosed(lazyMessage: () -> String) {
        check(!child.closed, lazyMessage)
        check(!parent.closed) { "Parent scope is closed" }
    }

    final override fun close() = child.close()
}