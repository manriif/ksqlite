package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.setBusyHandler].
 */
public fun interface BusyHandler {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/busy_handler.html).
     */
    public fun apply(count: Int): Int
}