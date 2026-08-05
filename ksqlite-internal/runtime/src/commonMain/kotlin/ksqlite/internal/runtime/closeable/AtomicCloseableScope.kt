package ksqlite.internal.runtime.closeable

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Thread-safe [CloseableScope].
 */
@OptIn(ExperimentalAtomicApi::class)
public open class AtomicCloseableScope : CloseableScope() {

    private val _closed: AtomicBoolean = AtomicBoolean(false)

    final override val closed: Boolean
        get() = _closed.load()

    final override fun close() {
        if (_closed.compareAndSet(expectedValue = false, newValue = true)) {
            onClose()
        }
    }
}