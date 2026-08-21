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

import ksqlite.capi.cipher.SqliteMcConfig
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3mc_config
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcConfigParamPrefix

internal class CipherConfigurationImpl(
    private val db: sqlite3?,
    private val scope: CloseableScope,
) : CipherConfiguration {

    override fun <Cipher : SqliteMcCipher> parameters(cipher: Cipher): CipherParameters<Cipher> =
        CipherParametersImpl(db, cipher, scope)

    /**
     * Returns the [Value] for the given [config].
     */
    private fun <Value : Any> getConfig(
        config: SqliteMcConfig<Value>,
        prefix: SqliteMcConfigParamPrefix
    ): Value = scope.notClosed {
        sqlite3mc_config(db, config, prefix) ?: throwParameterReadFailedCipherException()
    }

    /**
     * Sets the [value] for the given [config].
     */
    private fun <Value : Any> setConfig(
        config: SqliteMcConfig<Value>,
        prefix: SqliteMcConfigParamPrefix.ReadWrite,
        value: Value
    ) = scope.notClosed {
        val _ = sqlite3mc_config(db, config, prefix, value)
            ?: throwParameterWriteFailedCipherException()
    }

    override fun getCipher(prefix: SqliteMcConfigParamPrefix): SqliteMcCipher =
        getConfig(SqliteMcConfig.CIPHER, prefix)

    override fun <Cipher : SqliteMcCipher> setCipher(
        cipher: Cipher,
        prefix: SqliteMcConfigParamPrefix.ReadWrite,
        configure: (CipherParameters<Cipher>.() -> Unit)?
    ) = setConfig(SqliteMcConfig.CIPHER, prefix, cipher)

    override fun isHmacCheckEnabled(prefix: SqliteMcConfigParamPrefix): Boolean =
        getConfig(SqliteMcConfig.HMAC_CHECK, prefix) != 0

    override fun setHmacCheckEnabled(
        enabled: Boolean,
        prefix: SqliteMcConfigParamPrefix.ReadWrite
    ) = setConfig(SqliteMcConfig.HMAC_CHECK, prefix, if (enabled) 1 else 0)

    override fun isLegacyWalEnabled(prefix: SqliteMcConfigParamPrefix): Boolean =
        getConfig(SqliteMcConfig.MC_LEGACY_WAL, prefix) != 0

    override fun setLegacyWalEnabled(
        enabled: Boolean,
        prefix: SqliteMcConfigParamPrefix.ReadWrite
    ) = setConfig(SqliteMcConfig.MC_LEGACY_WAL, prefix, if (enabled) 1 else 0)
}