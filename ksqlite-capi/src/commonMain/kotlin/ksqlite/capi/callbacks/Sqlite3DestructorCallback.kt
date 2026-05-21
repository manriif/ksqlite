package ksqlite.capi.callbacks

/**
 * Generic callback invoked when an object is no longer needed by SQLite.
 */
public fun interface Sqlite3DestructorCallback<ClientData> {

    /**
     * Releases resource(s) associated with [clientData].
     */
    public fun handle(clientData: ClientData)
}