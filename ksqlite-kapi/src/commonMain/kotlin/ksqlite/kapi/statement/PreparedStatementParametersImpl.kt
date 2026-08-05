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

import ksqlite.capi.sqlite3_bind_blob
import ksqlite.capi.sqlite3_bind_blob64
import ksqlite.capi.sqlite3_bind_double
import ksqlite.capi.sqlite3_bind_int
import ksqlite.capi.sqlite3_bind_int64
import ksqlite.capi.sqlite3_bind_null
import ksqlite.capi.sqlite3_bind_parameter_count
import ksqlite.capi.sqlite3_bind_parameter_index
import ksqlite.capi.sqlite3_bind_parameter_name
import ksqlite.capi.sqlite3_bind_pointer
import ksqlite.capi.sqlite3_bind_text
import ksqlite.capi.sqlite3_bind_text64
import ksqlite.capi.sqlite3_bind_value
import ksqlite.capi.sqlite3_bind_zeroblob
import ksqlite.capi.sqlite3_bind_zeroblob64
import ksqlite.capi.sqlite3_clear_bindings
import ksqlite.capi.sqlite3_stmt
import ksqlite.kapi.buffer.Buffer
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.autoCloser
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.value.Value
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteTextEncoding

internal class PreparedStatementParametersImpl(
    private val stmt: sqlite3_stmt,
    private val scope: CloseableScope
) : PreparedStatementParameters {

    override val count: Int
        get() = scope.notClosed { sqlite3_bind_parameter_count(stmt) }

    override fun getIndex(name: String): Int =
        scope.notClosed { sqlite3_bind_parameter_index(stmt, name) }

    override fun getName(index: Int): String? =
        scope.notClosed { sqlite3_bind_parameter_name(stmt, index) }

    /**
     * Invokes [block] throwing [ksqlite.kapi.SQLiteException] if it returns a failure code.
     */
    private inline fun bind(block: () -> SqliteResultCode) =
        scope.notClosed { sqliteResultCheck(block()) }

    override fun bind(index: Int, value: Nothing?): Unit =
        bind { sqlite3_bind_null(stmt, index) }

    override fun bind(index: Int, value: Nothing?, size: Int) =
        bind { sqlite3_bind_zeroblob(stmt, index, size) }

    override fun bind(index: Int, value: Nothing?, size: ULong) =
        bind { sqlite3_bind_zeroblob64(stmt, index, size) }

    override fun bind(index: Int, value: ByteArray, size: Int) =
        bind { sqlite3_bind_blob(stmt, index, value, size, null) }

    override fun bind(
        index: Int,
        value: Buffer,
        size: Long,
        cleanup: ((Buffer) -> Unit)?
    ) = bind {
        sqlite3_bind_blob64(
            stmt = stmt,
            index = index,
            buffer = value.buffer,
            size = size,
            destroy = value.reference(cleanup)
        )
    }

    override fun bind(index: Int, value: Int) =
        bind { sqlite3_bind_int(stmt, index, value) }

    override fun bind(index: Int, value: Long) =
        bind { sqlite3_bind_int64(stmt, index, value) }

    override fun bind(index: Int, value: Double) =
        bind { sqlite3_bind_double(stmt, index, value) }

    override fun bind(index: Int, value: String) =
        bind { sqlite3_bind_text(stmt, index, value) }

    override fun bind(
        index: Int,
        value: Buffer,
        encoding: SqliteTextEncoding.BindText,
        size: Long,
        cleanup: ((Buffer) -> Unit)?
    ) = bind {
        sqlite3_bind_text64(
            stmt = stmt,
            index = index,
            buffer = value.buffer,
            size = size,
            encoding = encoding,
            destroy = value.reference(cleanup)
        )
    }

    override fun bind(index: Int, value: Value) =
        bind { sqlite3_bind_value(stmt, index, value.value) }

    override fun bind(index: Int, value: Any, type: String?) =
        bind { sqlite3_bind_pointer(stmt, index, value, type, autoCloser(value)) }

    override fun clear() = scope.notClosed {
        sqliteResultCheck(sqlite3_clear_bindings(stmt))
    }
}