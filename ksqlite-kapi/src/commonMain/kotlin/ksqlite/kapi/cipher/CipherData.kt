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

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.connection.SQLITE_MAIN_DB_NAME

/**
 * Reads per-connection [cipher data](https://utelle.github.io/SQLite3MultipleCiphers/docs/configuration/config_capi/#function-sqlite3mc_codec_data).
 *
 * The caller must close every [Buffer] returned here once done with it.
 */
public interface CipherData {

    /**
     * Returns the random cipher salt used for key derivation and stored in the database header of
     * [database], as a 32-byte hexadecimal-encoded string, or `null` if [database] does not exist
     * or the connection is not encrypted.
     */
    public fun cipherSalt(database: String = SQLITE_MAIN_DB_NAME): Buffer?

    /**
     * Returns the random cipher salt used for key derivation and stored in the database header of
     * [database], as a raw 16-byte value, or `null` if [database] does not exist or the connection
     * is not encrypted.
     */
    public fun cipherSaltRaw(database: String = SQLITE_MAIN_DB_NAME): Buffer?
}