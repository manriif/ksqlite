package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
public fun interface Sqlite3CreateCollationCallback<ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/create_collation.html).
     */
    public fun handle(
        clientData: ClientData,
        left: String,
        right: String
    ): Int
}