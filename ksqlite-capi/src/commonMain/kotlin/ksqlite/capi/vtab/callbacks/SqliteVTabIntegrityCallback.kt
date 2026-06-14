package ksqlite.capi.vtab.callbacks

import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * If the iVersion for an sqlite3_module is 4 or more and the xIntegrity method is not NULL, then
 * the PRAGMA integrity_check and PRAGMA quick_check commands will invoke xIntegrity as part of its
 * processing. If the xIntegrity method writes an error message string into the fifth parameter,
 * then PRAGMA integrity_check will report that error as part of its output. So, in other words, the
 * xIntegrity method allows the PRAGMA integrity_check command to verify the integrity of content
 * stored in a virtual table.
 *
 * The xIntegrity method should normally return SQLITE_OK - even if it finds problems in the
 * content of the virtual table. So, for example, if the inverted index for FTS5 is found to
 * be internally inconsistent, then the xIntegrity method should write an appropriate error
 * message into the pzErr parameter and return SQLITE_OK. But if the xIntegrity method is
 * unable to complete its evaluation of the virtual table content due to running out of
 * memory, then it should return SQLITE_NOMEM.
 *
 * [The xIntegrity Method](https://sqlite.org/vtab.html#the_xintegrity_method)
 */
public fun interface SqliteVTabIntegrityCallback<VTab : sqlite3_vtab> {

    /**
     * Result for [handle].
     */
    public sealed interface Result

    /**
     * Scope for [handle].
     */
    public sealed interface Scope {

        /**
         * Returns [SqliteResultCode.OK] to SQLite.
         *
         * If an error is found in the content on the virtual table, then [error] should be supplied
         * and is written to `pzErr`.
         */
        public fun success(error: String?): Result

        /**
         * Writes [error] to `pzErr` and returns [result] to SQLite.
         *
         * Calling this methods means that the xIntegrity method itself encountered problems while
         * trying to evaluate the virtual table content.
         */
        public fun failure(
            error: String,
            result: SqliteResultCode.Failure
        ): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xintegrity_method).
     */
    public fun Scope.handle(
        vTab: VTab,
        schema: String,
        tableName: String,
        flags: Int
    ): Result
}