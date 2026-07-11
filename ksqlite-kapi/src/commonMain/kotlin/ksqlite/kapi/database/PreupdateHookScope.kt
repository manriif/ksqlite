package ksqlite.kapi.database

import ksqlite.kapi.value.ProtectedValue

/**
 * Scope for use with [PreupdateHook.apply].
 */
public interface PreupdateHookScope {

    /**
     * Number of columns in the row that is being inserted, updated, or deleted.
     */
    public val count: Int

    /**
     * Deepness of the current change within trigger execution.
     */
    public val depth: Int

    /**
     * Returns the index if the column for the blob being written.
     */
    public val blobColumnIndex: Int

    /**
     * Returns the value of the column at [index] of the table row before it is updated.
     *
     * @throws ksqlite.kapi.SQLiteException if no value could be obtained.
     */
    public fun oldValue(index: Int): ProtectedValue

    /**
     * Returns the value of the column at [index] of the table row after it is updated.
     *
     * @throws ksqlite.kapi.SQLiteException if no value could be obtained.
     */
    public fun newValue(index: Int): ProtectedValue
}