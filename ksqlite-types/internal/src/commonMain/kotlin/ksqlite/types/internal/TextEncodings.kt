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
package ksqlite.types.internal

import ksqlite.types.SqliteTextEncoding

/**
 * Returns all [SqliteTextEncoding]s.
 */
private fun sqliteTextEncodings(): Set<SqliteTextEncoding> = setOf(
    SqliteTextEncoding.UTF8,
    SqliteTextEncoding.UTF8_ZT,
    SqliteTextEncoding.UFT16LE,
    SqliteTextEncoding.UTF16BE,
    SqliteTextEncoding.UTF16,
    SqliteTextEncoding.UTF16_ALIGNED,
)

/**
 * [SqliteTextEncoding]s associated by their integer value.
 */
@PublishedApi
internal val SqliteTextEncodings: Set<SqliteTextEncoding> = sqliteTextEncodings()

/**
 * Converts [encoding] into [SqliteTextEncoding].
 */
public inline fun <reified E : SqliteTextEncoding> convertTextEncoding(encoding: Int): E {
    val value = SqliteTextEncodings.firstOrNull { (encoding and it.value) == it.value }
    checkNotNull(value) { "Unknown SQLite text encoding: $encoding" }
    check(value is E) { "Unexpected encoding type: $value" }
    return value
}
