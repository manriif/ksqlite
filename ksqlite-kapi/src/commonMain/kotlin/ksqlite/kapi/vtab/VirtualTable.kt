package ksqlite.kapi.vtab

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.value.ProtectedValue

/**
 * Represents a [Virtual Table](https://sqlite.org/vtab.html).
 *
 * This interface declares only the mandatory functions required by SQLite for a virtual table.
 *
 * To supports optional functions, the implementation must also extend nested interfaces:
 * - xUpdate => [UpdateSupport]
 * - xFindFunction => [FindFunctionSupport]
 * - xBegin, xSync, xCommit, xRollback => [TransactionSupport]
 * - xRename => [RenameSupport]
 * - xSavepoint, xRelease, xRollbackTo => [NestedTransactionSupport]
 * - xIntegrity => [IntegritySupport]
 *
 * If an error is detected in a mandatory or optional function, and if error raising is allowed by
 * SQLite, it is allowed to raise an [SQLiteException] that is then reported to SQLite.
 */
public interface VirtualTable {

    ///////////////////////////////////////////////////////////////////////////
    // Mandatory functions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This method is used by SQLite to determine the best way to access the virtual table.
     */
    public fun VirtualTableBestIndexScope.bestIndex(info: IndexInfo)

    /**
     * Releases the connection to the virtual table without destroying the backing store, if any.
     */
    public fun disconnect()

    /**
     * Releases the connection to the virtual table and destroys the backing store, if any.
     */
    public fun destroy()

    /**
     * Creates a new cursor for accessing the virtual table.
     */
    public fun open(): VirtualTableCursor

    ///////////////////////////////////////////////////////////////////////////
    // Optional functions
    //
    // These interfaces must be implemented by an implementation of [VirtualTable] to enable
    // the related functions.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds support for the xUpdate function to the [VirtualTable].
     */
    public interface UpdateSupport {

        /**
         * Updates the virtual table and returns the newly inserted rowid or `null` if no rowid must
         * be returned to SQLite.
         */
        public fun VirtualTableUpdateScope.update(arguments: Array<ProtectedValue>): Long?
    }

    /**
     * Adds support for the xFindFunction function to the [VirtualTable].
     */
    public interface FindFunctionSupport {

        /**
         * Returns a [ScalarFunction] that overrides the global one identified by [name] and
         * [argumentCount]. If the function should not be overridden, then `null` is returned.
         */
        public fun findFunction(
            name: String,
            argumentCount: Int
        ): ScalarFunction?
    }

    /**
     * Adds support for the xBegin, xSync, xCommit and xRollback functions to the [VirtualTable].
     */
    public interface TransactionSupport {

        /**
         * Begins a transaction.
         */
        public fun begin()

        /**
         * Starts a two-phase commit.
         */
        public fun sync()

        /**
         * Commits the transaction.
         */
        public fun commit()

        /**
         * Rollbacks the transaction
         */
        public fun rollback()
    }

    /**
     * Adds support for the xRename function to the [VirtualTable].
     */
    public interface RenameSupport {

        /**
         * Renames the virtual table to [newName].
         */
        public fun rename(newName: String)
    }

    /**
     * Adds support for the xSavepoint, xRelease and xRollbackTo functions to the [VirtualTable].
     */
    public interface NestedTransactionSupport {

        /**
         * Saves the current state to [id].
         */
        public fun savepoint(id: Int)

        /**
         * Invalidates all savepoints >= [id].
         */
        public fun release(id: Int)

        /**
         * Restores the state to what it was when [savepoint] was called with [id], invalidating all
         * savepoints > [id].
         */
        public fun rollbackTo(id: Int)
    }

    /**
     * Adds support for the xIntegrity function to the [VirtualTable].
     */
    public interface IntegritySupport {

        /**
         * Verifies the integrity of the content stored in the virtual table.
         */
        public fun integrity(
            schema: String,
            tableName: String,
            flags: Int
        )
    }
}