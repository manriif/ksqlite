package ksqlite.kapi.impl.vtab

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.kapi.Cursor
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.impl.helpers.runCatchingSQLiteException
import ksqlite.kapi.vtab.VirtualTable
import ksqlite.kapi.vtab.VirtualTableCursor

/**
 * Implementation of [sqlite3_vtab].
 */
internal class VTab(
    internal val table: VirtualTable,
    internal val db: sqlite3
) : sqlite3_vtab() {

    /**
     * Sets the [errMsg] from [exception]'s message and returns [exception]'s result
     */
    fun handleError(exception: SQLiteException): Sqlite3Result.Failure {
        errMsg = exception.message
        return exception.result
    }

    /**
     * Executes [block] and returns [Sqlite3Result.OK] or an instance of [Sqlite3Result.Failure] if
     * [block]'s throws.
     */
    inline fun catching(block: VirtualTable.() -> Unit): Sqlite3Result.OkOrFailure {
        return table.runCatchingSQLiteException(::handleError) {
            block()
            Sqlite3Result.OK
        }
    }

    /**
     * Executes [block] and returns its result. If an error is thrown, it is passed to [transform]
     * and the result is returned.
     */
    inline fun <T> catching(
        transform: (Sqlite3Result.Failure) -> T,
        block: VirtualTable.() -> T
    ): T = table.runCatchingSQLiteException({ transform(handleError(it)) }) { block() }
}

/**
 * Implementation of [sqlite3_vtab_cursor].
 */
internal class VTabCursor(
    internal val cursor: VirtualTableCursor,
    internal val vTab: VTab,
) : sqlite3_vtab_cursor() {

    /**
     * Executes [block] and returns [Sqlite3Result.OK] or an instance of [Sqlite3Result.Failure] if
     * [block]'s throws.
     */
    inline fun catching(block: VirtualTableCursor.() -> Unit): Sqlite3Result.OkOrFailure {
        return cursor.runCatchingSQLiteException(vTab::handleError) {
            block()
            Sqlite3Result.OK
        }
    }

    /**
     * Executes [block] and returns its result. If an error is thrown, it is passed to [transform]
     * and the result is returned.
     */
    inline fun <T> catching(
        transform: (Sqlite3Result.Failure) -> T,
        block: VirtualTableCursor.() -> T
    ): T = cursor.runCatchingSQLiteException({ transform(vTab.handleError(it)) }) { block() }
}