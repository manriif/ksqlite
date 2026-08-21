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

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3mc_config_cipher
import ksqlite.internal.runtime.closeable.UnsafeCloseableScope
import ksqlite.kapi.throwSQLiteException
import ksqlite.types.cipher.SqliteMcCipher

internal class DynamicCipherCreateScopeImpl(
    private val db: sqlite3,
    cipherName: String
) : DynamicCipherCreateScope,
    UnsafeCloseableScope() {

    private val cipher = SqliteMcCipher.Dynamic(cipherName)

    override fun getParameter(name: String): Int = notClosed {
        sqlite3mc_config_cipher(db, cipher, SqliteMcCipher.Dynamic.Parameter(name), None)
            ?: throwSQLiteException("Cipher ${cipher.name} did not registered a $name parameter")
    }
}