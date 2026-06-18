package ksqlite.kapi.value

/**
 * Holder for a status-like value and its highwater.
 */
public data class StatusValue(
    val current: Long,
    val highwater: Long
)