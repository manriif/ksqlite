package ksqlite.kapi.statement

import ksqlite.capi.sqlite3_clear_bindings
import ksqlite.capi.sqlite3_column_count
import ksqlite.capi.sqlite3_db_handle
import ksqlite.capi.sqlite3_expanded_sql
import ksqlite.capi.sqlite3_finalize
import ksqlite.capi.sqlite3_reset
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.helpers.AtomicClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck

internal class StatementImpl(
    override val connection: DatabaseConnection,
    private val stmt: sqlite3_stmt
) : Statement,
    AtomicClosableScope() {

    init {
        check(sqlite3_db_handle(stmt) == connection.db)
    }

    override val columnCount: Int
        get() = notClosed { sqlite3_column_count(stmt) }

    override val expandedSql: String?
        get() = notClosed { sqlite3_expanded_sql(stmt) }

    override fun clear() =
        notClosed { sqliteResultCheck(sqlite3_clear_bindings(stmt)) }

    override fun reset() =
        notClosed { sqliteResultCheck(sqlite3_reset(stmt)) }

    override fun onClose() {
        sqliteResultCheck(sqlite3_finalize(stmt))
    }
}