package ksqlite.kapi.connection

/**
 * Callback to use with [Connection.setBusyHandler].
 */
public fun interface BusyHandler {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/busy_handler.html).
     */
    public fun apply(count: Int): Int
}