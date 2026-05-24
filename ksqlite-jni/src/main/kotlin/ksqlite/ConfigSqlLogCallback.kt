package ksqlite

/**
 * Callback for the CONFIG_SQLLOG option of [sqlite3_config].
 */
public fun interface ConfigSqlLogCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        db: Long,
        message: String?,
        messageType: Int
    )
}