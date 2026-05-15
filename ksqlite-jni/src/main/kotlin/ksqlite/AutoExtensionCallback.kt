package ksqlite

/**
 * Callback for auto extension.
 */
public fun interface AutoExtensionCallback {

    /**
     * @param pointer
     */
    public fun call(db: Long, api: Long): Int
}