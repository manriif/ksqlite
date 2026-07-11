package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_progress_handler].
 */
public fun interface SqliteProgressHandlerCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/progress_handler.html).
     */
    public fun apply(appData: AppData): Int
}