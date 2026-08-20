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

import ksqlite.kapi.buffer.ReadableBuffer
import ksqlite.kapi.value.UnprotectedValue
import ksqlite.types.SqliteDataType

/**
 * Exposes the API to extract values from an SQLite row.
 *
 * SQLite may perform an implicit type conversion to match the requested value type. If conversion
 * is not feasible then `null` is returned.
 */
public interface Row {

    /**
     * Returns the number of column in the current row of the result set.
     */
    public val dataCount: Int

    /**
     * Returns the name of the database for the column at [index].
     */
    public fun getDatabaseName(index: Int): String?

    /**
     * Returns the name of the table for the column at [index].
     */
    public fun getTableName(index: Int): String?

    /**
     * Returns the name of the column at [index].
     */
    public fun getColumnOriginName(index: Int): String?

    /**
     * Returns the name of the column at [index] as it was assigned in the result set.
     */
    public fun getColumnName(index: Int): String?

    /**
     * Returns the declared type of the value at [index].
     */
    public fun getDeclaredType(index: Int): String?

    /**
     * Returns the default data type of the value at [index].
     */
    public fun getType(index: Int): SqliteDataType

    /**
     * Returns the size of the column at [index] in bytes.
     */
    public fun getSize(index: Int): Int

    /**
     * Returns the value of the column at [index] as a [ByteArray] or `null` in the value cannot be
     * converted to a [ByteArray].
     */
    public fun getByteArray(index: Int): ByteArray?

    /**
     * Returns the value of the column at [index] as a [ReadableBuffer] or `null` in the value
     * cannot be converted to a [ReadableBuffer].
     */
    public fun getBuffer(index: Int): ReadableBuffer?

    /**
     * Returns the value of the column at [index] as a [Double].
     */
    public fun getDouble(index: Int): Double

    /**
     * Returns the value of the column at [index] as an [Int].
     */
    public fun getInt(index: Int): Int

    /**
     * Returns the value of the column at [index] as a [Long].
     */
    public fun getLong(index: Int): Long

    /**
     * Returns the value of the column at [index] as a [String] or `null` in the value cannot be
     * converted to a [String].
     */
    public fun getString(index: Int): String?

    /**
     * Returns the value of the column at [index] as an [UnprotectedValue ]or `null` in the value
     * cannot be converted to a [ByteArray].
     */
    public fun getValue(index: Int): UnprotectedValue?
}