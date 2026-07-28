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
package ksqlite.capi.cipher

/**
 * Parameters supported by
 * [sqlite3mc_codec_data](https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/#function-sqlite3mc_codec_data).
 */
public enum class SqliteMcCodecDataParam(
    internal val paramName: String,
    internal val bufferSize: Int
) {

    /**
     * The random cipher salt used for key derivation and stored in the database header (as a
     * hexadecimal encoded string, 32 bytes).
     */
    CIPHER_SALT("cipher_salt", 32),

    /**
     * The random cipher salt used for key derivation and stored in the database header (as a raw
     * binary string, 16 bytes).
     */
    CIPHER_SALT_RAW("raw:cipher_salt", 16)
}