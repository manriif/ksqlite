package ksqlite.kapi.statement

public interface Statement {

    /**
     * Resets all host parameters to `null`.
     */
    public fun clearBindings()
}