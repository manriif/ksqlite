package ksqlite.kapi.helpers

import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Helper for object implementing [AutoCloseable].
 */
@PublishedApi
internal abstract class BaseClosableScope : AutoCloseable {

    @PublishedApi
    internal abstract val closed: Boolean

    /**
     * Notifies about scope closing.
     */
    open fun onClose() = Unit

    /**
     * Throws [IllegalStateException] if [closed] is `true`.
     */
    internal fun ensureNotClosed(lazyMessage: () -> String = { "Scope is closed" }) {
        check(!closed, lazyMessage)
    }

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
}

/**
 * Unsafe [BaseClosableScope].
 */
@PublishedApi
internal open class ClosableScope : BaseClosableScope() {

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
 * Subclass of [ClosableScope] invoking [onClose] when the scope is closed.
 */
internal class DelegatingCloseableScope(
    private val onClose: () -> Unit
) : ClosableScope() {

    override fun onClose() {
        onClose.invoke()
    }
}

/**
 * Helper for objects implementing [AutoCloseable].
 */
@PublishedApi
@OptIn(ExperimentalAtomicApi::class)
internal open class AtomicClosableScope : BaseClosableScope() {

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