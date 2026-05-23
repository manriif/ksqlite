package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_rollback_hook].
 */
public fun interface Sqlite3RollbackHookCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/commit_hook.html).
     */
    public fun handle(appData: AppData)
}