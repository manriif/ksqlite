package ksqlite

/**
 * Callback for auto extension.
 */
public fun interface AutoExtensionCallback {

    /**
     * Invoked from JNI.
     *
     * If an error is encountered, then a [KsqliteJniException] should be thrown with the expected
     * error message and the result code to be returned by the JNI call.
     * .
     * @param dbPtr pointer to the sqlite3 struct.
     * @param apiPtr pointer to the sqlite3_api_routines struct.
     */
    public fun call(
        dbPtr: Long,
        apiPtr: Long
    ): Int
}