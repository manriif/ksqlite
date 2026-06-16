package ksqlite.kapi.database

/**
 * Holder for database status
 */
public data class DatabaseConnectionStatus(
    val current: Long,
    val highwater: Long
)