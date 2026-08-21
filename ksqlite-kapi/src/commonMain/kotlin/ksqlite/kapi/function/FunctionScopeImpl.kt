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
package ksqlite.kapi.function

import ksqlite.capi.sqlite3_context
import ksqlite.capi.sqlite3_context_db_handle
import ksqlite.capi.sqlite3_result_error
import ksqlite.capi.sqlite3_result_error_code
import ksqlite.capi.sqlite3_result_error_nomem
import ksqlite.capi.sqlite3_result_error_toobig
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.helpers.ContextCloseableScope
import ksqlite.kapi.sqliteRequireConnection
import ksqlite.kapi.throwSQLiteException
import ksqlite.types.SqliteResultCode

/**
 * Special exception types used to set the result of a function call.
 */
private sealed class ResultException : SQLiteException(SqliteResultCode.ERROR, "") {
    class NoMem : ResultException()
    class TooBig : ResultException()
}

@PublishedApi
internal class FunctionScopeImpl(context: sqlite3_context) :
    FunctionScope,
    ContextCloseableScope(context) {

    override val connection: DatabaseConnection
        get() = notClosed { sqliteRequireConnection(sqlite3_context_db_handle(context)) }

    override fun resultError(
        message: String,
        result: SqliteResultCode.Failure
    ): Nothing = notClosed { throwSQLiteException(message, result) }

    override fun resultErrorNoMem(): Nothing = notClosed { throw ResultException.NoMem() }

    override fun resultErrorTooBig(): Nothing = notClosed { throw ResultException.TooBig() }

    fun handleError(exception: SQLiteException) {
        when (exception) {
            is ResultException -> when (exception) {
                is ResultException.NoMem -> sqlite3_result_error_nomem(context)
                is ResultException.TooBig -> sqlite3_result_error_toobig(context)
            }

            else -> {
                sqlite3_result_error(context, exception.message)
                sqlite3_result_error_code(context, exception.result)
            }
        }
    }
}