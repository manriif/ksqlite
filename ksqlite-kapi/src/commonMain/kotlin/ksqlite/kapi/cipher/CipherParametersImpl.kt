package ksqlite.kapi.cipher

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3mc_config_cipher
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcConfigCipherParam
import ksqlite.types.cipher.SqliteMcConfigParamPrefix

internal class CipherParametersImpl<Cipher : SqliteMcCipher>(
    private val db: sqlite3?,
    private val cipher: Cipher,
    private val scope: ClosableScope
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