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

import ksqlite.capi.sqlite3_column_count
import ksqlite.capi.sqlite3_db_handle
import ksqlite.capi.sqlite3_expanded_sql
import ksqlite.capi.sqlite3_sql
import ksqlite.capi.sqlite3_stmt
import ksqlite.capi.sqlite3_stmt_busy
import ksqlite.capi.sqlite3_stmt_isexplain
import ksqlite.capi.sqlite3_stmt_readonly
import ksqlite.capi.sqlite3_stmt_status
import ksqlite.internal.runtime.closeable.AtomicCloseableScope
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.sqliteRequireConnection
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteStatementStatusCounter
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal open class ReadOnlyPreparedStatement(val stmt: sqlite3_stmt) :
    PreparedStatementBase,
    AutoCloseable,
    AtomicCloseableScope() {

    override val connection: DatabaseConnection by lazy {
        notClosed {
            sqliteRequireConnection(checkNotNull(sqlite3_db_handle(stmt)) {
                "Could not find the connection associated with the statement"
            })
        }
    }

    final override val columnCount: Int
        get() = notClosed { sqlite3_column_count(stmt) }

    final override val expandedSql: String?
        get() = notClosed { sqlite3_expanded_sql(stmt) }

    final override val sql: String
        get() = notClosed { sqlite3_sql(stmt) }

    final override val isBusy: Boolean
        get() = notClosed { sqlite3_stmt_busy(stmt) != 0 }

    override val explain: SqliteExplainMode
        get() = notClosed { sqlite3_stmt_isexplain(stmt) }

    final override val isReadOnly: Boolean
        get() = notClosed { sqlite3_stmt_readonly(stmt) != 0 }

    final override fun getStatus(counter: SqliteStatementStatusCounter): Int =
        notClosed { sqlite3_stmt_status(stmt, counter, 0) }
}