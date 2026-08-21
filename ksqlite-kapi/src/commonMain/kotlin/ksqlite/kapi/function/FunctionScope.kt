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

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.types.SqliteResultCode

/**
 * Supplies the APIs available during the invocation of a function hook.
 *
 * If an error is detected, an [SQLiteException] can be thrown, which is equivalent to calling
 * [resultError]. Only [SQLiteException] is recognized as a normal error, any other exception
 * type is not caught and propagates instead. [resultErrorNoMem] and [resultErrorTooBig]
 * are message-less, allocation-free alternatives SQLite provides for two failure classes it
 * treats specially, see their own documentation.
 */
public interface FunctionScope {

    /**
     * Database connection associated with the hook.
     */
    public val connection: DatabaseConnection

    /**
     * Fails the function call with [message]. Equivalent to throwing an [SQLiteException] with
     * [result] as its error code.
     */
    public fun resultError(
        message: String,
        result: SqliteResultCode.Failure = SqliteResultCode.ERROR
    ): Nothing

    /**
     * Fails the function call, indicating that a memory allocation failed. Unlike [resultError],
     * this does not accept a message and performs no allocation of its own, so it remains safe to
     * call even when memory is already exhausted.
     */
    public fun resultErrorNoMem(): Nothing

    /**
     * Fails the function call with a canned "string or BLOB too big" error. Unlike [resultError],
     * this does not accept a message, since the failure is already self-explanatory and does not
     * need one formatted.
     */
    public fun resultErrorTooBig(): Nothing
}