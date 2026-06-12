package ksqlite.kapi.vtab

import ksqlite.capi.types.Sqlite3ConflictResolutionMode
import ksqlite.kapi.value.ProtectedValue

/**
 * Scope to use with [VirtualTable.UpdateSupport.update].
 */
public interface VirtualTableUpdateScope {

    /**
     * Returns the virtual table conflict policy.
     */
    public val onConflict: Sqlite3ConflictResolutionMode

    /**
     * Returns `true` if and only if the column corresponding to X is unchanged by the UPDATE
     * operation that the xUpdate method call was invoked to implement and if the prior xColumn
     * method call that was invoked to extract the value for that column returned without setting a
     * result (probably because it queried sqlite3_vtab_nochange() and found that the column was
     * unchanging).
     */
    public val ProtectedValue.nochange: Boolean
}