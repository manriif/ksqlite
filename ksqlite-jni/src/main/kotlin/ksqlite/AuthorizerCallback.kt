package ksqlite

/**
 * Callback for use with [sqlite3_set_authorizer].
 */
public fun interface AuthorizerCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(
        opId: Int,
        string1: String?,
        string2: String?,
        string3: String?,
        string4: String?
    ): Int
}