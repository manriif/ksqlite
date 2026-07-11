package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.sqlite3_exec].
 */
public fun interface SqliteExecCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/exec.html).
     *
     * The application must not access more than [columnCount] elements from [columnValues] and
     * [columnNames] even if they contain more than [columnCount] elements.
     */
    public fun apply(
        appData: AppData,
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    ): Int
}