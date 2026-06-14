package ksqlite.capi.callbacks

/**
 * Callback to use with [ksqlite.capi.types.CapiSqliteConfigOption.LOG].
 */
public fun interface SqliteConfigLogCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/c_config_covering_index_scan.html).
     */
    public fun apply(
        appData: AppData,
        errorCode: Int,
        message: String?
    )
}