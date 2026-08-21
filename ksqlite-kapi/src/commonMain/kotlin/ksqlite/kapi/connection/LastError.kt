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
package ksqlite.kapi.connection

import ksqlite.types.SqliteResultCode

/**
 * Provides access to the most recent database error information.
 *
 * Note that value returned by the member properties may change while the database connection is
 * used.
 */
public interface LastError {

    /**
     * UTF-8 encoded English language explanation.
     */
    public val message: String?

    /**
     * Error code.
     */
    public val code: SqliteResultCode

    /**
     * Extended error code.
     */
    public val extendedCode: SqliteResultCode

    /**
     * Byte offset of the start of the token specified by the input SQL, if referenced by the most
     * recent error.
     */
    public val offset: Int

    /**
     * OS-dependent error code or error number that caused the most recent I/O error or failure to
     * open a file.
     */
    public val systemError: Int

    /**
     * Sets the [code] to [errorCode] and [message] to [errorMessage].
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurs.
     */
    public fun update(
        errorCode: SqliteResultCode,
        errorMessage: String? = null
    )
}