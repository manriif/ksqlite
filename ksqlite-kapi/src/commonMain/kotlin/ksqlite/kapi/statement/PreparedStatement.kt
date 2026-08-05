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
package ksqlite.kapi.statement

import ksqlite.capi.sqlite3_stmt
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteStatementStatusCounter

/**
 * Exposes the [Statement](https://sqlite.org/c3ref/stmt.html) API.
 */
public abstract class PreparedStatement internal constructor(): AutoCloseable {

    /**
     * Statement handle.
     */
    internal abstract val stmt: sqlite3_stmt

    /**
     * Database connection to which this statement belongs.
     */
    public abstract val connection: DatabaseConnection

    /**
     * Statement parameters.
     */
    public abstract val parameters: PreparedStatementParameters

    /**
     * Number of column in the result set.
     */
    public abstract val columnCount: Int

    /**
     * SQL associated with the statement with bound parameters expanded.
     */
    public abstract val expandedSql: String?

    /**
     * SQL text used to create this statement.
     */
    public abstract val sql: String

    /**
     * Whether the prepared statement has been stepped at least once using [step] but has neither
     * run to completion (returned `null` from [step]) nor been reset using [reset].
     */
    public abstract val isBusy: Boolean

    /**
     * Explain mode.
     *
     * @throws ksqlite.kapi.SQLiteException if the setting cannot be changed in the actual state.
     */
    public abstract var explain: SqliteExplainMode

    /**
     * Whether the prepared statement makes no direct changes to the content of the database file.
     */
    public abstract val isReadOnly: Boolean

    /**
     * Evaluates the statement and returns a [Row] if any data is found or `null` if the statement
     * has finished executing.
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurs while executing the statement.
     */
    @IgnorableReturnValue
    public abstract fun step(): Row?

    /**
     * Returns the current value of the given [counter].
     */
    public abstract fun getStatus(
        counter: SqliteStatementStatusCounter,
        reset: Boolean
    ): Int

    /**
     * Resets the prepared statement object back to its initial state, making it ready to be
     * re-executed.
     *
     * Bindings values are not affected, [clear] must be used instead.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public abstract fun reset()

    /**
     * Deletes a prepared statement.
     *
     * @throws ksqlite.kapi.SQLiteException if finalizing the statement fails.
     */
    abstract override fun close()
}