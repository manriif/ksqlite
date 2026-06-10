package ksqlite.kapi.vtab

/**
 * Represents a virtual table.
 * This interface declares only the mandatory functions required by SQLite for a virtual table.
 *
 * To supports optional functions, the implementation must also extend nested interfaces:
 * - xUpdate => [UpdateSupport]
 * - xFindFunction => [FindFunctionSupport]
 * - xBegin, xSync, xCommit, xRollback => [TransactionSupport]
 * - xRename => [RenameSupport]
 * - xSavepoint, xRelease, xRollbackTo => [NestedTransactionSupport]
 * - xIntegrity => [IntegritySupport]
 */
public interface VirtualTable<Cursor: VirtualTableCursor> {

    ///////////////////////////////////////////////////////////////////////////
    // Optional functions
    //
    // These interfaces must be implemented by an implementation of [VirtualTable] to enable
    // the related functions.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds support for the xUpdate function to the [VirtualTableModule].
     */
    public interface UpdateSupport<Cursor> {

    }

    /**
     * Adds support for the xFindFunction function to the [VirtualTableModule].
     */
    public interface FindFunctionSupport<> {

    }

    /**
     * Adds support for the xBegin, xSync, xCommit and xRollback functions to the
     * [VirtualTableModule].
     */
    public interface TransactionSupport

    /**
     * Adds support for the xRename function to the [VirtualTableModule].
     */
    public interface RenameSupport

    /**
     * Adds support for the xSavepoint, xRelease and xRollbackTo functions to the
     * [VirtualTableModule].
     */
    public interface NestedTransactionSupport

    /**
     * Adds support for the xIntegrity function to the [VirtualTableModule].
     */
    public interface IntegritySupport
}