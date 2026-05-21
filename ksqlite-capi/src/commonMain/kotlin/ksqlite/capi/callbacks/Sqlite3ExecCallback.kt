package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_exec].
 */
public fun interface Sqlite3ExecCallback<ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/exec.html).
     */
    public fun handle(
        clientData: ClientData,
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    ): Int
}