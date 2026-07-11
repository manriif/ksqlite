package ksqlite.kapi.value

internal data class StatusImpl(
    override val current: Long,
    override val highwater: Long
): Status