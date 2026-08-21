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
package ksqlite.kapi.cipher

import ksqlite.capi.cipher.SqliteMcCodecDataParam
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3mc_codec_data
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.Buffer.Companion.wrap
import ksqlite.internal.runtime.closeable.CloseableScope

internal class CipherDataImpl(
    private val db: sqlite3,
    private val scope: CloseableScope
) : CipherData {

    /**
     * Returns the codec data for [param].
     */
    private fun getCodecData(
        param: SqliteMcCodecDataParam,
        database: String,
    ): Buffer? = scope.notClosed { sqlite3mc_codec_data(db, database, param)?.wrap() }

    override fun cipherSalt(database: String): Buffer? =
        getCodecData(CIPHER_SALT, database)

    override fun cipherSaltRaw(database: String): Buffer? =
        getCodecData(CIPHER_SALT_RAW, database)
}