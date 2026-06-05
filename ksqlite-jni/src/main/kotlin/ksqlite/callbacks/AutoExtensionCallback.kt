package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.ksqlite_auto_extension].
 */
public fun interface AutoExtensionCallback {

    /**
     * Invoked from JNI.
     *
     * If an error is encountered, then a [ksqlite.KsqliteJniException] should be thrown with the expected
     * error message and the result code to be returned by the JNI call.
     */
    public fun apply(
        dbPtr: Long,
        apiPtr: Long
    ): Int
}