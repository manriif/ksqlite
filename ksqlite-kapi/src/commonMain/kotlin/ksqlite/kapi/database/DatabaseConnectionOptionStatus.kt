package ksqlite.kapi.database

/**
 * Holder for the values of a database status option.
 */
public data class DatabaseConnectionOptionStatus(
    val current: Long,
    val highwater: Long
)