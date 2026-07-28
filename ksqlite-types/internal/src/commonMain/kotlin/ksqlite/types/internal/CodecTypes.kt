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

import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcCodecType

/**
 * Returns all [SqliteMcCodecType]s.
 */
@Suppress("DEPRECATION")
private fun sqliteMcCodecTypes(): Set<SqliteMcCodecType> =
    setOf(
        SqliteMcCodecType.AES128,
        SqliteMcCodecType.AES256,
        SqliteMcCodecType.CHACHA20,
        SqliteMcCodecType.SQLCIPHER,
        SqliteMcCodecType.RC4,
        SqliteMcCodecType.ASCON128,
        SqliteMcCodecType.AEGIS,
    )

/**
 * [SqliteMcCodecType]s associated by their integer code.
 */
@PublishedApi
internal val SqliteMcCodecTypeMap: Map<String, SqliteMcCodecType> =
    sqliteMcCodecTypes().associateBy(SqliteMcCodecType::name)

/**
 * Converts [name] to [SqliteMcCipher].
 */
public fun convertMcCipher(name: String): SqliteMcCipher =
    SqliteMcCodecTypeMap[name] ?: SqliteMcCipher.Dynamic(name)