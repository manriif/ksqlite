package ksqlite.kapi.callbacks

/**
 * Callback to use with [ksqlite.kapi.ConnectionInitializer.busyHandler].
 */
public fun interface BusyHandler {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/busy_handler.html).
     */
    public fun apply(count: Int): Int
}