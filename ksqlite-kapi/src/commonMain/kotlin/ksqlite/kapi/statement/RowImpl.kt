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

import ksqlite.capi.sqlite3_column_blob
import ksqlite.capi.sqlite3_column_buffer
import ksqlite.capi.sqlite3_column_bytes
import ksqlite.capi.sqlite3_column_database_name
import ksqlite.capi.sqlite3_column_decltype
import ksqlite.capi.sqlite3_column_double
import ksqlite.capi.sqlite3_column_int
import ksqlite.capi.sqlite3_column_int64
import ksqlite.capi.sqlite3_column_name
import ksqlite.capi.sqlite3_column_origin_name
import ksqlite.capi.sqlite3_column_table_name
import ksqlite.capi.sqlite3_column_text
import ksqlite.capi.sqlite3_column_type
import ksqlite.capi.sqlite3_column_value
import ksqlite.capi.sqlite3_data_count
import ksqlite.capi.sqlite3_stmt
import ksqlite.kapi.buffer.ReadableBuffer
import ksqlite.kapi.helpers.UnsafeClosableScope
import ksqlite.kapi.value.UnprotectedValue
import ksqlite.kapi.value.toUnprotectedValue
import ksqlite.types.SqliteDataType

internal class RowImpl(private val stmt: sqlite3_stmt) :
    Row,
    UnsafeClosableScope() {

    override val dataCount: Int
        get() = notClosed { sqlite3_data_count(stmt) }

    override fun getDatabaseName(index: Int): String? =
        notClosed { sqlite3_column_database_name(stmt, index) }

    override fun getTableName(index: Int): String? =
        notClosed { sqlite3_column_table_name(stmt, index) }

    override fun getColumnOriginName(index: Int): String? =
        notClosed { sqlite3_column_origin_name(stmt, index) }

    override fun getColumnName(index: Int): String? =
        notClosed { sqlite3_column_name(stmt, index) }

    override fun getDeclaredType(index: Int): String? =
        notClosed { sqlite3_column_decltype(stmt, index) }

    override fun getType(index: Int): SqliteDataType =
        notClosed { sqlite3_column_type(stmt, index) }

    override fun getBytes(index: Int): Int =
        notClosed { sqlite3_column_bytes(stmt, index) }

    override fun getBlob(index: Int): ByteArray? =
        notClosed { sqlite3_column_blob(stmt, index) }

    override fun getBuffer(index: Int): ReadableBuffer? = notClosed {
        sqlite3_column_buffer(stmt, index)?.let { ReadableBuffer(it, this) }
    }

    override fun getDouble(index: Int): Double =
        notClosed { sqlite3_column_double(stmt, index) }

    override fun getInt(index: Int): Int =
        notClosed { sqlite3_column_int(stmt, index) }

    override fun getLong(index: Int): Long =
        notClosed { sqlite3_column_int64(stmt, index) }

    override fun getString(index: Int): String? =
        notClosed { sqlite3_column_text(stmt, index) }

    override fun getValue(index: Int): UnprotectedValue? = notClosed {
        sqlite3_column_value(stmt, index)?.toUnprotectedValue(this)
    }
}