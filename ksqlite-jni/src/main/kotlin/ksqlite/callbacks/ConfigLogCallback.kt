package ksqlite.callbacks

/**
 * Callback for use with the CONFIG_LOG option of [ksqlite.sqlite3_config].
 */
public fun interface ConfigLogCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        errorCode: Int,
        message: String?
    )
}