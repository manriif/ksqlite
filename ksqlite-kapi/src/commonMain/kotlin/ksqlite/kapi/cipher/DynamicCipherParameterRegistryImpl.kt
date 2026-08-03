package ksqlite.kapi.cipher

import ksqlite.kapi.helpers.UnsafeClosableScope

internal class DynamicCipherParameterRegistryImpl(
    private val callbacks: MutableList<DynamicCipherParameter.() -> Unit>
) : DynamicCipherParameterRegistry,
    UnsafeClosableScope() {

    /**
     * Registers a new [DynamicCipherParameter] and [configure]s it.
     */
    override fun register(configure: DynamicCipherParameter.() -> Unit): Unit =
        notClosed { callbacks.add(configure) }
}