package ksqlite.capi.callbacks

import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Callback to use with [ksqlite.capi.sqlite3_busy_handler].
 */
public fun interface Sqlite3BusyHandlerCallback<ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/busy_handler.html).
     */
    public fun handle(
        clientData: ClientData,
        count: Int
    ): Int
}