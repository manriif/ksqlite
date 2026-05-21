package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_autovacuum_pages].
 */
public fun interface Sqlite3AutoVacuumPagesCallback<ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/autovacuum_pages.html).
     */
    public fun handle(
        clientData: ClientData,
        schemaName: String,
        dbPage: UInt,
        freePage: UInt,
        bytePerPage: UInt
    ): UInt
}