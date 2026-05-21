package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_commit_hook].
 */
public fun interface Sqlite3CommitHookCallback<ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/commit_hook.html).
     */
    public fun handle(clientData: ClientData): Int
}