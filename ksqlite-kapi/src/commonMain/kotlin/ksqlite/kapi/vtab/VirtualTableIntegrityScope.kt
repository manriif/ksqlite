package ksqlite.kapi.vtab

/**
 * Scope to use with [VirtualTable.integrity].
 */
public interface VirtualTableIntegrityScope {

    /**
     * Notifies SQLite that a problem was found with the virtual table content.
     * Note that this replaces any [message] from a previous call to [report].
     */
    public fun report(message: String)
}