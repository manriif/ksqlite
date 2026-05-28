package ksqlite

/**
 * Callback for use with the CONFIG_LOG option of [sqlite3_config].
 */
public fun interface ConfigLogCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        errorCode: Int,
        message: String?
    )
}