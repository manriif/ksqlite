package ksqlite.capi.callbacks

/**
 * Generic callback invoked when an object is no longer needed by SQLite.
 */
public fun interface SqliteDestroyCallback<AppData> {

    /**
     * Releases resource(s) associated with [appData].
     */
    public fun apply(appData: AppData)
}