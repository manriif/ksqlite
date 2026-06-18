package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.setProgressHandler].
 */
public fun interface ProgressHandler {

    /**
     * Details on result can be found [here](https://sqlite.org/c3ref/progress_handler.html).
     *
     * To interrupt the operation, `true` must be returned, `false` to keep it continue.
     */
    public fun apply(): Boolean
}