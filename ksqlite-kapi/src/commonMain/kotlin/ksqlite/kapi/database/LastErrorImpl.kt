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
package ksqlite.kapi.database

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_errcode
import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.sqlite3_error_offset
import ksqlite.capi.sqlite3_extended_errcode
import ksqlite.capi.sqlite3_set_errmsg
import ksqlite.capi.sqlite3_system_errno
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.types.SqliteResultCode

internal class LastErrorImpl(
    private val db: sqlite3,
    private val scope: CloseableScope
) : LastError {

    override val message: String?
        get() = scope.notClosed { sqlite3_errmsg(db) }

    override val code: SqliteResultCode
        get() = scope.notClosed { sqlite3_errcode(db) }

    override val extendedCode: SqliteResultCode
        get() = scope.notClosed { sqlite3_extended_errcode(db) }

    override val offset: Int
        get() = scope.notClosed { sqlite3_error_offset(db) }

    override val systemError: Int
        get() = scope.notClosed { sqlite3_system_errno(db) }

    override fun update(
        errorCode: SqliteResultCode,
        errorMessage: String?
    ) = scope.notClosed { sqliteResultCheck(sqlite3_set_errmsg(db, errorCode, errorMessage)) }
}