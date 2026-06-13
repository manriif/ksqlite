package ksqlite.kapi.vtab

import ksqlite.capi.types.vtab.Sqlite3IndexInfo
import ksqlite.capi.types.vtab.Sqlite3VTab
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.impl.vtab.VTab
import ksqlite.kapi.value.ProtectedValue

/**
 * Represents a [Virtual Table](https://sqlite.org/vtab.html).
 *
 * This interface declares the mandatory functions required by SQLite as well as the optional ones.
 * An optional function `F` must be implemented only if the [VirtualTableModule] that instantiated a
 * [VirtualTable] returned the function `F` when [VirtualTableModule.optionalFunctions] was called.
 * Otherwise, the function `F` do not need to be implemented and is guaranteed to never been called
 * by SQLite.
 *
 * If an error is detected in a mandatory or optional function, and if error raising is allowed by
 * SQLite, it is allowed to raise an [SQLiteException] that is then reported to SQLite.
 *
 * The [errMsg] is internally filled with the [SQLiteException]'s message when one is thrown by a
 * function. So unless the implementation do not want to deal with exceptions, it is not required to
 * fill it.
 *
 * The [nRef] and [errMsg] properties must only be accessed from within one of the methods listed in
 * [VirtualTable]. So it is illegal to access these fields in one of [VirtualTableModule] methods as
 * an example.
 */
public abstract class VirtualTable : Sqlite3VTab {

    /**
     * This field is only available between [VirtualTableModule.connect] /
     * [VirtualTableModule.Regular.create] (excluded) and [disconnect] / [destroy] (included).
     */
    internal var parent: Sqlite3VTab? = null

    final override val nRef: Int
        get() = parent(Sqlite3VTab::nRef)

    final override var errMsg: String?
        get() = parent(Sqlite3VTab::errMsg)
        set(value) = parent { errMsg = value }

    /**
     * Invokes [block] with the attached [VTab] or throws if [VTab] is detached.
     */
    private inline fun <R> parent(block: Sqlite3VTab.() -> R): R =
        checkNotNull(parent) { "Virtual table is not attached" }.block()

    ///////////////////////////////////////////////////////////////////////////
    // Mandatory functions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This method is used by SQLite to determine the best way to access the virtual table.
     */
    public abstract fun VirtualTableBestIndexScope.bestIndex(info: Sqlite3IndexInfo)

    /**
     * Releases the connection to the virtual table without destroying the backing store, if any.
     */
    public abstract fun disconnect()

    /**
     * Releases the connection to the virtual table and destroys the backing store, if any.
     */
    public abstract fun destroy()

    /**
     * Creates a new cursor for accessing the virtual table.
     */
    public abstract fun open(): VirtualTableCursor

    ///////////////////////////////////////////////////////////////////////////
    // Optional functions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Throws a [NotImplementedError] for an optional function for which a [VirtualTableModule]
     * specified that it is supported.
     */
    private fun notImplemented(name: String): Nothing = throw NotImplementedError(
        "Optional virtual table function $name() is not implemented but the associated module " +
                "specified that it supports it"
    )

    /**
     * Updates the virtual table and returns the newly inserted rowid or `null` if no rowid must
     * be returned to SQLite.
     */
    public open fun VirtualTableUpdateScope.update(arguments: Array<ProtectedValue>): Long? =
        notImplemented("update")

    /**
     * Returns a [ScalarFunction] that overrides the global one identified by [name] and
     * [argumentCount]. If the function should not be overridden, then `null` is returned.
     */
    public open fun VirtualTableFindFunctionScope.findFunction(
        name: String,
        argumentCount: Int
    ): ScalarFunction? = notImplemented("findFunction")

    /**
     * Begins a transaction.
     */
    public open fun begin(): Unit = notImplemented("begin")

    /**
     * Starts a two-phase commit.
     */
    public open fun sync(): Unit = notImplemented("sync")

    /**
     * Commits the transaction.
     */
    public open fun commit(): Unit = notImplemented("commit")

    /**
     * Rollbacks the transaction.
     */
    public open fun rollback(): Unit = notImplemented("rollback")

    /**
     * Renames the virtual table to [newName].
     */
    public open fun rename(newName: String): Unit = notImplemented("rename")

    /**
     * Saves the current state to [id].
     */
    public open fun savepoint(id: Int): Unit = notImplemented("savepoint")

    /**
     * Invalidates all savepoints >= [id].
     */
    public open fun release(id: Int): Unit = notImplemented("release")

    /**
     * Restores the state to what it was when [savepoint] was called with [id], invalidating all
     * savepoints > [id].
     */
    public open fun rollbackTo(id: Int): Unit = notImplemented("rollbackTo")

    /**
     * Verifies the integrity of the content stored in the virtual table.
     *
     * Note that an error that is found on the virtual table content can be reported to SQLite by
     * setting an appropriate error message in [errMsg].
     */
    public open fun VirtualTableIntegrityScope.integrity(
        schema: String,
        tableName: String,
        flags: Int
    ): Unit = notImplemented("integrity")
}