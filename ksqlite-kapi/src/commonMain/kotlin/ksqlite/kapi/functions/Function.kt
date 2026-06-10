package ksqlite.kapi.functions

/**
 * [Application-Defined SQL Functions](https://sqlite.org/appfunc.html).
 */
public interface Function {

    /**
     * Called when the function is finalized by SQLite.
     */
    public fun destroy(): Unit = Unit
}