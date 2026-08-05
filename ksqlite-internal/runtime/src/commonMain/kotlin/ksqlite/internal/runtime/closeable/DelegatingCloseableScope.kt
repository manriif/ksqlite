package ksqlite.internal.runtime.closeable

/**
 * Subclass of [UnsafeCloseableScope] invoking [onClose] when the scope is closed.
 */
public class DelegatingCloseableScope(
    private val onClose: () -> Unit
) : UnsafeCloseableScope() {

    override fun onClose() {
        onClose.invoke()
    }
}