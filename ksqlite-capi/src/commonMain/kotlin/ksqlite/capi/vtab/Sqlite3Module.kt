@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.vtab

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

/**
 * Definition of [sqlite3_module].
 *
 * Exception throwing is prohibited in all of this interface methods' implementation.
 */
public interface Sqlite3Module<AppData, VTab : Sqlite3VTab, VTabCursor : Sqlite3VTabCursor> {

    /**
     * Defines the particular edition of the module table structure. Currently, iVersion is always
     * 4 or less, but in future releases of SQLite the module structure definition might be extended
     * with additional methods and in that case the maximum iVersion value will be increased.
     */
    public val iVersion: Sqlite3ModuleVersion

    /**
     * Returns a list of optional methods that are not supported by this virtual table module.
     * Thoses methods won't be called by SQLite.
     * 
     * A method that is not in the returned list must be implemented or an exception is thrown
     * when it gets called.
     */
    public fun unsupportedOptionalMethods(): List<Sqlite3VTabOptionalMethod>

    /**
     * The xCreate method is called to create a new instance of a virtual table in response to a
     * CREATE VIRTUAL TABLE statement. If the xCreate method is the same pointer as the xConnect
     * method, then the virtual table is an eponymous virtual table. If the xCreate method is
     * omitted (if it is a NULL pointer) then the virtual table is an eponymous-only virtual table.
     */
    public fun CreateScope<VTab>.create(
        db: sqlite3,
        appData: AppData,
        argv: Array<String>
    ): CreateResult<VTab>

    /**
     * The xConnect method is very similar to xCreate. It has the same parameters and constructs a
     * new sqlite3_vtab structure just like xCreate. And it must also call sqlite3_declare_vtab()
     * like xCreate. It should also make all of the same sqlite3_vtab_config() calls as xCreate.
     *
     * The difference is that xConnect is called to establish a new connection to an existing
     * virtual table whereas xCreate is called to create a new virtual table from scratch.
     */
    public fun ConnectScope<VTab>.connect(
        db: sqlite3,
        appData: AppData,
        argv: Array<String>
    ): ConnectResult<VTab>

    /**
     * SQLite uses the xBestIndex method of a virtual table module to determine the best way to
     * access the virtual table.
     */
    public fun bestIndex(
        vTab: VTab,
        info: sqlite3_index_info
    ): Sqlite3Result.OkOrFailure

    /**
     * This method releases a connection to a virtual table. Only the sqlite3_vtab object is
     * destroyed. The virtual table is not destroyed and any backing store associated with the
     * virtual table persists. This method undoes the work of xConnect.
     */
    public fun disconnect(vTab: VTab): Sqlite3Result.OkOrFailure

    /**
     * This method releases a connection to a virtual table, just like the xDisconnect method, and
     * it also destroys the underlying table implementation. This method undoes the work of xCreate.
     */
    public fun destroy(vTab: VTab): Sqlite3Result.OkOrFailure

    /**
     * The xOpen method creates a new cursor used for accessing (read and/or writing) a virtual
     * table. A successful invocation of this method will allocate the memory for the
     * sqlite3_vtab_cursor (or a subclass), initialize the new object, and make *ppCursor point to
     * the new object.
     */
    public fun OpenScope<VTabCursor>.open(vTab: VTab): OpenResult<VTabCursor>

    /**
     * The xClose method closes a cursor previously opened by xOpen. The SQLite core will always
     * call xClose once for each cursor opened using xOpen.
     */
    public fun close(cursor: VTabCursor): Sqlite3Result.OkOrFailure

    /**
     * This method begins a search of a virtual table. The first argument is a cursor opened by
     * xOpen. The next two arguments define a particular search index previously chosen by
     * xBestIndex. The specific meanings of idxNum and idxStr are unimportant as long as xFilter and
     * xBestIndex agree on what that meaning is.
     */
    public fun filter(
        cursor: VTabCursor,
        idxNum: Int,
        idxStr: String?,
        argv: Array<sqlite3_value>
    ): Sqlite3Result.OkOrFailure

    /**
     * The xNext method advances a virtual table cursor to the next row of a result set initiated by
     * xFilter. If the cursor is already pointing at the last row when this routine is called, then
     * the cursor no longer points to valid data and a subsequent call to the xEof method must
     * return true (non-zero). If the cursor is successfully advanced to another row of content,
     * then subsequent calls to xEof must return false (zero).
     */
    public fun next(cursor: VTabCursor): Sqlite3Result.OkOrFailure

    /**
     * The xEof method must return false (zero) if the specified cursor currently points to a valid
     * row of data, or true (non-zero) otherwise. This method is called by the SQL engine
     * immediately after each xFilter and xNext invocation.
     */
    public fun eof(cursor: VTabCursor): Sqlite3Result.OkOrFailure

    /**
     * The SQLite core invokes this method in order to find the value for the N-th column of the
     * current row. N is zero-based so the first column is numbered 0. The xColumn method may return
     * its result back to SQLite using one of the sqlite3_result_x() api family.
     */
    public fun column(
        cursor: VTabCursor,
        context: sqlite3_context,
        n: Int
    ): Sqlite3Result.OkOrFailure

    /**
     * A successful invocation of this method will cause *pRowid to be filled with the rowid of row
     * that the virtual table cursor pCur is currently pointing at. This method returns SQLITE_OK on
     * success. It returns an appropriate error code on failure.
     */
    public fun RowidScope.rowid(cursor: VTabCursor): RowidResult

    /**
     * All changes to a virtual table are made using the xUpdate method. This one method can be used
     * to insert, delete, or update.
     */
    public fun UpdateScope.update(
        vTab: VTab,
        argv: Array<sqlite3_value>
    ): UpdateResult = unsupportedMethod()

    /**
     * This method begins a transaction on a virtual table. This method is optional. The xBegin
     * pointer of sqlite3_module may be NULL.
     *
     * This method is always followed by one call to either the xCommit or xRollback method. Virtual
     * table transactions do not nest, so the xBegin method will not be invoked more than once on a
     * single virtual table without an intervening call to either xCommit or xRollback. Multiple
     * calls to other methods can and likely will occur in between the xBegin and the corresponding
     * xCommit or xRollback.
     */
    public fun begin(vTab: VTab): Sqlite3Result.OkOrFailure = unsupportedMethod()

    int (*xBegin)(sqlite3_vtab *pVTab);
    int (*xSync)(sqlite3_vtab *pVTab);
    int (*xCommit)(sqlite3_vtab *pVTab);
    int (*xRollback)(sqlite3_vtab *pVTab);
    int (*xFindFunction)(sqlite3_vtab *pVtab, int nArg, const char *zName,
    void (**pxFunc)(sqlite3_context*,int,sqlite3_value**),
    void **ppArg);
    int (*xRename)(sqlite3_vtab *pVtab, const char *zNew);
    /* The methods above are in version 1 of the sqlite_module object. Those
    ** below are for version 2 and greater. */
    int (*xSavepoint)(sqlite3_vtab *pVTab, int);
    int (*xRelease)(sqlite3_vtab *pVTab, int);
    int (*xRollbackTo)(sqlite3_vtab *pVTab, int);
    /* The methods above are in versions 1 and 2 of the sqlite_module object.
    ** Those below are for version 3 and greater. */
    int (*xShadowName)(const char*);
    /* The methods above are in versions 1 through 3 of the sqlite_module object.
    ** Those below are for version 4 and greater. */
    int (*xIntegrity)(sqlite3_vtab *pVTab, const char *zSchema,
    const char *zTabName, int mFlags, char **pzErr);

    ///////////////////////////////////////////////////////////////////////////
    // Results
    ///////////////////////////////////////////////////////////////////////////

    public sealed interface ConnectResult<VTab : Sqlite3VTab>

    public sealed interface CreateResult<VTab : Sqlite3VTab>

    public sealed interface OpenResult<VTabCursor: Sqlite3VTabCursor>

    public sealed interface RowidResult

    public sealed interface UpdateResult

    ///////////////////////////////////////////////////////////////////////////
    // Scopes
    ///////////////////////////////////////////////////////////////////////////

    public sealed interface ConnectScope<VTab : Sqlite3VTab> {

        public fun success(vTab: VTab): CreateResult<VTab>

        public fun failure(error: String): CreateResult<VTab>
    }

    public sealed interface CreateScope<VTab : Sqlite3VTab> : ConnectScope<VTab> {

        public fun eponymous(): CreateResult<VTab>

        public fun eponymousOnly(): CreateResult<VTab>
    }

    public sealed interface OpenScope<VTabCursor: Sqlite3VTabCursor> {

        public fun success(cursor: VTabCursor): OpenResult<VTabCursor>

        public fun failure(result: Sqlite3Result.Failure): OpenResult<VTabCursor>
    }

    public sealed interface RowidScope {

        public fun success(rowid: Long): RowidResult

        public fun failure(result: Sqlite3Result.Failure): RowidResult
    }

    public sealed interface UpdateScope {

        /**
         * Ignored for update other than insert.
         */
        public fun success(rowid: Long): UpdateResult

        public fun failure(result: Sqlite3Result.Failure): UpdateResult
    }
}

/**
 * Throws an [UnsupportedOperationException].
 *
 * Used for [Sqlite3Module]'s methods that are optional and that implementor may not implement.
 * Such method should never be called by SQLite.
 */
private fun unsupportedMethod(): Nothing {
    throw UnsupportedOperationException("Method is not supported !")
}