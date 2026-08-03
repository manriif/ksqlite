package ksqlite.kapi.cipher

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3mc_config_cipher
import ksqlite.kapi.helpers.UnsafeClosableScope
import ksqlite.types.cipher.SqliteMcCipher

internal class DynamicCipherCreateScopeImpl(
    private val db: sqlite3,
    cipherName: String
) : DynamicCipherCreateScope,
    UnsafeClosableScope() {

    private val cipher = SqliteMcCipher.Dynamic(cipherName)

    override fun getParameter(name: String): Int = notClosed {
        sqlite3mc_config_cipher(db, cipher, SqliteMcCipher.Dynamic.Parameter(name), None)
            ?: throwCipherException(
                "Cipher ${cipher.name} did not registered a parameter with name $name "
            )
    }
}