package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_busy_handler].
 */
public fun interface SqliteBusyHandlerCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/busy_handler.html).
     */
    public fun apply(
        appData: AppData,
        count: Int
    ): Int
}