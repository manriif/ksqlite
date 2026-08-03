package ksqlite.kapi.cipher

import ksqlite.capi.cipher.SqliteMcCodecDataParam
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3mc_codec_data
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.Buffer.Companion.wrap
import ksqlite.kapi.helpers.ClosableScope

internal class CipherDataImpl(
    private val db: sqlite3,
    private val scope: ClosableScope
) : CipherData {

    /**
     * Returns the codec data for [param].
     */
    private fun getCodecData(
        param: SqliteMcCodecDataParam,
        database: String?,
    ): Buffer? = scope.notClosed { sqlite3mc_codec_data(db, database, param)?.wrap() }

    override fun cipherSalt(database: String?): Buffer? =
        getCodecData(CIPHER_SALT, database)

    override fun cipherSaltRaw(database: String?): Buffer? =
        getCodecData(CIPHER_SALT_RAW, database)
}