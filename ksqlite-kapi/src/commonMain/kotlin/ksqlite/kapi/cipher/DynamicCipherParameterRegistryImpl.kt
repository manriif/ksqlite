package ksqlite.kapi.cipher

import ksqlite.internal.runtime.closeable.UnsafeCloseableScope

internal class DynamicCipherParameterRegistryImpl(
    private val callbacks: MutableList<DynamicCipherParameter.() -> Unit>
) : DynamicCipherParameterRegistry,
    UnsafeCloseableScope() {

    /**
     * Registers a new [DynamicCipherParameter] and [configure]s it.
     */
    override fun register(configure: DynamicCipherParameter.() -> Unit): Unit =
        notClosed { callbacks.add(configure) }
}