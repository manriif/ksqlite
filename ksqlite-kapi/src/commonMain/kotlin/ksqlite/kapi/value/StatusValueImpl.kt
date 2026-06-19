package ksqlite.kapi.value

internal data class StatusValueImpl(
    override val current: Long,
    override val highwater: Long
): StatusValue