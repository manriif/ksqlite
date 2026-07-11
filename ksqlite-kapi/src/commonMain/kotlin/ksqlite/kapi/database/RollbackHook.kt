package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.setRollbackHook].
 */
public fun interface RollbackHook {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/commit_hook.html).
     */
    public fun apply()
}