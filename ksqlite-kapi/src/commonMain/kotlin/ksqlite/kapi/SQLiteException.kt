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
package ksqlite.kapi

import ksqlite.types.SqliteResultCode

/**
 * Exception resulting from a call to an SQLite API that failed, returning a non-successful result
 * code.
 *
 * This exception can also be thrown in an SQLite callback, the [result] and the error [message]
 * are then forwarded to SQLite.
 *
 * The helper function [throwSQLiteException] can be used to construct and throw an instance of
 * [SQLiteException].
 */
public open class SQLiteException(
    /**
     * The result returned by the API call that failed.
     */
    public val result: SqliteResultCode.Failure,
    override val message: String
) : RuntimeException(message)

///////////////////////////////////////////////////////////////////////////
// Factories
///////////////////////////////////////////////////////////////////////////

/**
 * Throws an [SQLiteException] with supplied [message] and [result] which is default to
 * [SqliteResultCode.ERROR].
 */
public fun throwSQLiteException(
    message: String,
    result: SqliteResultCode.Failure = SqliteResultCode.ERROR
): Nothing {
    throw SQLiteException(result, message)
}