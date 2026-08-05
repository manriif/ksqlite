package ksqlite.internal.runtime.closeable

/**
 * Subclass of [AtomicCloseableScope] invoking [onClose] when the scope is closed.
 */
public class DelegatingAtomicCloseableScope(
    private val onClose: () -> Unit
) : AtomicCloseableScope() {

    override fun onClose() {
        onClose.invoke()
    }
}