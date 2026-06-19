package ksqlite.kapi.function

/**
 * [Application-Defined SQL Functions](https://sqlite.org/appfunc.html).
 */
public interface Function : AutoCloseable {

    /**
     * Called when the function is finalized by SQLite. Finalization can also happen when the
     * function registration fails.
     */
    public override fun close(): Unit = Unit
}