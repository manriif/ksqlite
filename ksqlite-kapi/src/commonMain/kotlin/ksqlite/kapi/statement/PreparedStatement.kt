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

import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteStatementStatusCounter

/**
 * Exposes the [Statement](https://sqlite.org/c3ref/stmt.html) API.
 */
public sealed interface PreparedStatement :
    PreparedStatementBase,
    AutoCloseable {

    /**
     * Statement parameters.
     */
    public val parameters: PreparedStatementParameters

    /**
     * Explain mode.
     *
     * @throws ksqlite.kapi.SQLiteException if the setting cannot be changed in the actual state.
     */
    public override var explain: SqliteExplainMode

    /**
     * Evaluates the statement and returns a [Row] if any data is found or `null` if the statement
     * has finished executing.
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurs while executing the statement.
     */
    @IgnorableReturnValue
    public fun step(): Row?

    /**
     * Returns the current value of the given [counter].
     */
    public fun getStatus(
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
    public fun reset()

    /**
     * Deletes a prepared statement.
     *
     * @throws ksqlite.kapi.SQLiteException if finalizing the statement fails.
     */
    override fun close()
}