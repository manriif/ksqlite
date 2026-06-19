package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.setTrace].
 */
public fun interface Trace {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/trace_v2.html).
     */
    public fun apply(event: TraceEvent)
}