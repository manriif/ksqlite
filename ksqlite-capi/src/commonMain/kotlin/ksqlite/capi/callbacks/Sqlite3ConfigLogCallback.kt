package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.types.Sqlite3ConfigOption.LOG].
 */
public fun interface Sqlite3ConfigLogCallback<ClientData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/c_config_covering_index_scan.html).
     */
    public fun handle(
        clientData: ClientData,
        errorCode: Int,
        errorMsg: String?
    )
}