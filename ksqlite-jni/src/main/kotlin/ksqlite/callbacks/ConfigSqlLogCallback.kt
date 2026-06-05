package ksqlite.callbacks

/**
 * Callback for use with the CONFIG_SQLLOG option of [ksqlite.sqlite3_config].
 */
public fun interface ConfigSqlLogCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        db: Long,
        message: String?,
        messageType: Int
    )
}