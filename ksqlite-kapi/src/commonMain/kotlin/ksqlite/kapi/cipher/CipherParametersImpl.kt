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
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcConfigCipherParam
import ksqlite.types.cipher.SqliteMcConfigParamPrefix

internal class CipherParametersImpl<Cipher : SqliteMcCipher>(
    private val db: sqlite3?,
    private val cipher: Cipher,
    private val scope: CloseableScope
) : CipherParameters<Cipher> {

    override fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> get(
        param: Param,
        prefix: SqliteMcConfigParamPrefix
    ): Value = scope.notClosed {
        sqlite3mc_config_cipher(db, cipher, param, prefix)
            ?: throwParameterReadFailedCipherException()
    }

    override fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> set(
        param: Param,
        value: Value,
        prefix: SqliteMcConfigParamPrefix.ReadWrite
    ) = scope.notClosed {
        val _ = sqlite3mc_config_cipher(db, cipher, param, prefix, value)
            ?: throwParameterWriteFailedCipherException()
    }
}