/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.kapi.vtab

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.function.ScalarFunction
import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.vtab.SqliteIndexInfo
import ksqlite.types.vtab.SqliteVtab

/**
 * Represents a [Virtual Table](https://sqlite.org/vtab.html).
 *
 * This interface declares the mandatory functions required by SQLite as well as the optional ones.
 * An optional function `F` must be overridden only if it is listed in the
 * [VirtualTableModule.optionalFunctions] of the [VirtualTableModule] that instantiated this
 * [VirtualTable]. Otherwise `F` does not need to be overridden and is guaranteed to never be
 * called by SQLite.
 *
 * If an error is detected in a mandatory or optional function, it is allowed to raise an
 * [SQLiteException] that is then reported to SQLite.
 *
 * [errMsg] is automatically filled with the thrown [SQLiteException]'s message, so setting it
 * manually is only needed when not relying on exceptions to report errors.
 *
 * [nRef] and [errMsg] can only be accessed from within one of this [VirtualTable]'s own methods,
 * not from a [VirtualTableModule] method such as [VirtualTableModule.connect].
 */
public abstract class VirtualTable : SqliteVtab {

    /**
     * This field is only available between [VirtualTableModule.connect] /
     * [VirtualTableModule.Regular.create] (excluded) and [disconnect] / [destroy] (included).
     */
    internal var parent: SqliteVtab? = null

    final override val nRef: Int
        get() = parent(SqliteVtab::nRef)

    final override var errMsg: String?
        get() = parent(SqliteVtab::errMsg)
        set(value) = parent { errMsg = value }

    /**
     * Invokes [block] with the attached [Vtab] or throws if [Vtab] is detached.
     */
    private inline fun <R> parent(block: SqliteVtab.() -> R): R =
        checkNotNull(parent) { "Virtual table is not attached" }.block()

    ///////////////////////////////////////////////////////////////////////////
    // Required hooks
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called by SQLite to determine the most efficient way to access the virtual table for a
     * given query, described by [info].
     */
    public abstract fun VirtualTableBestIndexScope.bestIndex(info: SqliteIndexInfo)

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
    // Optional hooks
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Throws [VirtualTableOptionalFunctionNotImplementedError] for [function].
     */
    private fun notImplemented(function: VirtualTableOptionalFunction): Nothing =
        throw VirtualTableOptionalFunctionNotImplementedError(function)

    /**
     * Applies an INSERT, UPDATE or DELETE described by [arguments] to the virtual table.
     * Returns the rowid of the newly inserted row, or `null` if none should be reported to
     * SQLite. Conflict resolution and unchanged-column information are available through the
     * receiver [VirtualTableUpdateScope].
     */
    public open fun VirtualTableUpdateScope.update(arguments: Array<ProtectedValue>): Long? =
        notImplemented(VirtualTableOptionalFunction.Update)

    /**
     * Returns a [ScalarFunction] that overrides the global one identified by [name] and
     * [argumentCount]. If the function should not be overridden, then `null` is returned.
     */
    public open fun VirtualTableFindFunctionScope.findFunction(
        name: String,
        argumentCount: Int
    ): ScalarFunction? = notImplemented(VirtualTableOptionalFunction.FindFunction)

    /**
     * Begins a transaction.
     */
    public open fun begin(): Unit = notImplemented(VirtualTableOptionalFunction.Begin)

    /**
     * Starts a two-phase commit.
     */
    public open fun sync(): Unit = notImplemented(VirtualTableOptionalFunction.Sync)

    /**
     * Commits the transaction.
     */
    public open fun commit(): Unit = notImplemented(VirtualTableOptionalFunction.Commit)

    /**
     * Rollbacks the transaction.
     */
    public open fun rollback(): Unit = notImplemented(VirtualTableOptionalFunction.Rollback)

    /**
     * Renames the virtual table to [newName].
     */
    public open fun rename(newName: String): Unit = notImplemented(VirtualTableOptionalFunction.Rename)

    /**
     * Records a savepoint identified by [id].
     */
    public open fun savepoint(id: Int): Unit = notImplemented(VirtualTableOptionalFunction.Savepoint)

    /**
     * Invalidates all savepoints >= [id].
     */
    public open fun release(id: Int): Unit = notImplemented(VirtualTableOptionalFunction.Release)

    /**
     * Restores the state to what it was when [savepoint] was called with [id], invalidating all
     * savepoints > [id].
     */
    public open fun rollbackTo(id: Int): Unit = notImplemented(VirtualTableOptionalFunction.RollbackTo)

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
    ): Unit = notImplemented(VirtualTableOptionalFunction.Integrity)
}