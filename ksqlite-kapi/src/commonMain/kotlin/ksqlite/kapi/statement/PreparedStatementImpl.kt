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
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.kapi.statement

import ksqlite.capi.sqlite3_finalize
import ksqlite.capi.sqlite3_reset
import ksqlite.capi.sqlite3_step
import ksqlite.capi.sqlite3_stmt
import ksqlite.capi.sqlite3_stmt_explain
import ksqlite.capi.sqlite3_stmt_status
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.connection.impl
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.sqliteResultThrow
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteStatementStatusCounter
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal class PreparedStatementImpl(
    stmt: sqlite3_stmt,
    override val connection: DatabaseConnection,
    private val listener: Listener
) : PreparedStatement,
    ReadOnlyPreparedStatement(stmt) {

    private val lastRow = AtomicReference<RowImpl?>(null)
    override val parameters = PreparedStatementParametersImpl(stmt, this)

    override var explain: SqliteExplainMode
        get() = super.explain
        set(value) = notClosed { sqliteResultCheck(sqlite3_stmt_explain(stmt, value)) }

    override fun step(): Row? = notClosed {
        when (val code = sqlite3_step(stmt)) {
            ROW -> RowImpl(stmt).also { row ->
                lastRow.exchange(row)?.close()
            }

            DONE -> {
                lastRow.exchange(null)?.close()
                null
            }

            is Failure -> sqliteResultThrow(code, connection.impl.db)
            else -> error("Unexpected SQLite result code: $code") // SQLITE_OK
        }
    }

    override fun getStatus(counter: SqliteStatementStatusCounter, reset: Boolean): Int =
        notClosed { sqlite3_stmt_status(stmt, counter, if (reset) 1 else 0) }

    override fun reset(): Unit = notClosed {
        val result = sqlite3_reset(stmt)
        lastRow.exchange(null)?.close()

        if (result is Failure) {
            sqliteResultThrow(result, connection.impl.db)
        }
    }

    override fun onClose() {
        val result = sqlite3_finalize(stmt)
        lastRow.exchange(null)?.close()
        listener.onStatementClosed(this)

        if (result is Failure) {
            sqliteResultThrow(result, connection.impl.db)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Listener for statement events.
     */
    fun interface Listener {

        /**
         * Notifies about a statement being closed.
         */
        fun onStatementClosed(statement: PreparedStatementImpl)
    }
}