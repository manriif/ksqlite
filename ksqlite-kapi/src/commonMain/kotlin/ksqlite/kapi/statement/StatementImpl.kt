package ksqlite.kapi.statement

import ksqlite.capi.sqlite3_clear_bindings
import ksqlite.capi.sqlite3_column_count
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.kapi.helpers.AtomicClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck

internal class StatementImpl(private val stmt: sqlite3_stmt) : Statement, AtomicClosableScope() {

    override val columnCount: Int
        get() = notClosed { sqlite3_column_count(stmt) }

    /**
     * Resets all host parameters to `null`.
     */
    override fun clearBindings(): Unit =
        notClosed { sqliteResultCheck(sqlite3_clear_bindings(stmt)) }


}