package ksqlite.kapi.helpers

import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Helper for object implementing [AutoCloseable].
 */
@PublishedApi
internal abstract class BaseClosableScope: AutoCloseable {

    @PublishedApi
    internal abstract val closed: Boolean

    /**
     * Notifies about scope closing.
     */
    open fun onClose() = Unit

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
 * Helper for objects implementing [AutoCloseable].
 */
@PublishedApi
@OptIn(ExperimentalAtomicApi::class)
internal open class AtomicClosableScope : BaseClosableScope() {

    private val _closed = AtomicBoolean(false)

    override val closed: Boolean
        get() = _closed.load()

    final override fun close() {
        if (_closed.compareAndSet(expectedValue = false, newValue = true)) {
            onClose()
        }
    }
}