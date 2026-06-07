package ksqlite.callbacks

/**
 * Callback for use with [ksqlite.sqlite3_set_authorizer].
 */
public fun interface AuthorizerCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        opId: Int,
        string1: String?,
        string2: String?,
        string3: String?,
        string4: String?
    ): Int
}