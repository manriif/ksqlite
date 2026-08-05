package ksqlite.internal.runtime.closeable

import kotlin.concurrent.Volatile

/**
 * Unsafe [CloseableScope].
 */
public open class UnsafeCloseableScope : CloseableScope() {

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