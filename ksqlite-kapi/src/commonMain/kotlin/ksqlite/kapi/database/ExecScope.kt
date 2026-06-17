package ksqlite.kapi.database

/**
 * Scope to use with [Exec.apply].
 */
public interface ExecScope {

    /**
     * Aborts the execution and cancels subsequent SQL statements.
     */
    public fun abort(): Nothing
}