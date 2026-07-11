package ksqlite.kapi.vtab

import ksqlite.types.SqliteResultCode
import ksqlite.capi.sqlite3
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.helpers.runCatchingSQLiteException

/**
 * Implementation of [sqlite3_vtab].
 */
internal class Vtab(
    internal val table: VirtualTable,
    internal val db: sqlite3
) : sqlite3_vtab() {

    /**
     * Sets the [errMsg] from [exception]'s message and returns [exception]'s result
     */
    fun handleError(exception: SQLiteException): SqliteResultCode.Failure {
        errMsg = exception.message
        return exception.result
    }

    /**
     * Executes [block] and returns [SqliteResultCode.OK] or an instance of [SqliteResultCode.Failure] if
     * [block]'s throws.
     */
    inline fun catching(block: VirtualTable.() -> Unit): SqliteResultCode.OkOrFailure {
        return table.runCatchingSQLiteException(::handleError) {
            block()
            SqliteResultCode.OK
        }
    }

    /**
     * Executes [block] and returns its result. If an error is thrown, it is passed to [transform]
     * and the result is returned.
     */
    inline fun <T> catching(
        transform: (SqliteResultCode.Failure) -> T,
        block: VirtualTable.() -> T
    ): T = table.runCatchingSQLiteException({ transform(handleError(it)) }) { block() }
}

/**
 * Implementation of [sqlite3_vtab_cursor].
 */
internal class VtabCursor(
    internal val cursor: VirtualTableCursor,
    internal val vTab: Vtab,
) : sqlite3_vtab_cursor() {

    /**
     * Executes [block] and returns [SqliteResultCode.OK] or an instance of [SqliteResultCode.Failure] if
     * [block]'s throws.
     */
    inline fun catching(block: VirtualTableCursor.() -> Unit): SqliteResultCode.OkOrFailure {
        return cursor.runCatchingSQLiteException(vTab::handleError) {
            block()
            SqliteResultCode.OK
        }
    }

    /**
     * Executes [block] and returns its result. If an error is thrown, it is passed to [transform]
     * and the result is returned.
     */
    inline fun <T> catching(
        transform: (SqliteResultCode.Failure) -> T,
        block: VirtualTableCursor.() -> T
    ): T = cursor.runCatchingSQLiteException({ transform(vTab.handleError(it)) }) { block() }
}