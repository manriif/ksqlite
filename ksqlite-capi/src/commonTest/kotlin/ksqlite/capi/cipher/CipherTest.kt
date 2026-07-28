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

import ksqlite.capi.findVfs
import ksqlite.capi.runSqliteConnectionTest
import ksqlite.capi.runSqliteTest
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_close
import ksqlite.capi.sqlite3_exec
import ksqlite.capi.sqlite3_key
import ksqlite.capi.sqlite3_open_v2
import ksqlite.capi.sqlite3_rekey
import ksqlite.capi.sqlite3mc_cipher_count
import ksqlite.capi.sqlite3mc_cipher_index
import ksqlite.capi.sqlite3mc_cipher_name
import ksqlite.capi.sqlite3mc_codec_data
import ksqlite.capi.sqlite3mc_config
import ksqlite.capi.sqlite3mc_config_cipher
import ksqlite.capi.sqlite3mc_version
import ksqlite.capi.usingRealTempFile
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcCodecType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests the SQLite3 Multiple Ciphers builtin cipher related functions.
 */
class CipherTest {

    @Test
    fun cipherRegistryWorks() = runSqliteTest {
        val version = sqlite3mc_version()
        assertEquals("SQLite3 Multiple Ciphers 2.5.0", version)

        val cipherCount = sqlite3mc_cipher_count()
        assertTrue(cipherCount >= 6)

        val chacha20Index = sqlite3mc_cipher_index(SqliteMcCodecType.CHACHA20)
        assertTrue(chacha20Index > 0)

        val chacha20Name = sqlite3mc_cipher_name(chacha20Index)
        assertEquals("chacha20", chacha20Name)

        val aes256Index = sqlite3mc_cipher_index(SqliteMcCodecType.AES256)
        assertTrue(aes256Index > 0)

        val aes256Name = sqlite3mc_cipher_name(aes256Index)
        assertEquals("aes256cbc", aes256Name)

        val unknownCipher = SqliteMcCipher.Dynamic("doesNotExist")
        val unknownIndex = sqlite3mc_cipher_index(unknownCipher)
        assertEquals(-1, unknownIndex)

        val outOfRangeName = sqlite3mc_cipher_name(cipherCount + 1)
        assertNull(outOfRangeName)
    }

    @Test
    fun globalConfigIsReadOnlyWithoutDb() = runSqliteTest {
        val defaultCipher = sqlite3mc_config(null, SqliteMcConfig.CIPHER, None)
        assertEquals(SqliteMcCodecType.CHACHA20, defaultCipher)

        val minHmacCheck =
            sqlite3mc_config(null, SqliteMcConfig.HMAC_CHECK, Min)

        assertEquals(0, minHmacCheck)

        val maxHmacCheck =
            sqlite3mc_config(null, SqliteMcConfig.HMAC_CHECK, Max)

        assertEquals(1, maxHmacCheck)
    }

    @Test
    fun perConnectionConfigWorks() = runSqliteConnectionTest { db ->
        val hmacCheck = sqlite3mc_config(db, SqliteMcConfig.HMAC_CHECK, None)
        assertEquals(1, hmacCheck)

        val disabledHmacCheck = sqlite3mc_config(db, SqliteMcConfig.HMAC_CHECK, None, 0)
        assertEquals(0, disabledHmacCheck)

        val readBackHmacCheck = sqlite3mc_config(db, SqliteMcConfig.HMAC_CHECK, None)
        assertEquals(0, readBackHmacCheck)

        val kdfIter = sqlite3mc_config_cipher(
            db = db,
            cipher = SqliteMcCodecType.CHACHA20,
            param = SqliteMcCodecType.CHACHA20.KDF_ITER,
            prefix = None
        )
        assertNotNull(kdfIter)
        assertTrue(kdfIter > 0)

        val newKdfIter = 32_000

        val updatedKdfIter = sqlite3mc_config_cipher(
            db = db,
            cipher = SqliteMcCodecType.CHACHA20,
            param = SqliteMcCodecType.CHACHA20.KDF_ITER,
            prefix = None,
            newValue = newKdfIter
        )
        assertEquals(newKdfIter, updatedKdfIter)
    }

    @Test
    fun keyRekeyRoundTripWorks() = runSqliteTest {
        findVfs().usingRealTempFile("cipher.db") { path ->
            val originalKey = "correct horse battery staple".encodeToByteArray()
            val rotatedKey = "new rotated passphrase".encodeToByteArray()

            val createSql = "CREATE TABLE fruits(id INTEGER, name TEXT);"
            val insertSql = "INSERT INTO fruits VALUES (1, 'Mangue');"
            val selectSql = "SELECT name FROM fruits WHERE id = 1;"

            // Create the encrypted database, pinning the cipher explicitly instead of relying on
            // whatever cipher happens to be the compile-time default.
            val outCreateDb = sqlite3.OutputParam()

            val createOpenResult = sqlite3_open_v2(
                fileName = path,
                outDb = outCreateDb,
                flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.CREATE,
                vfs = null
            )
            assertEquals(OK, createOpenResult)

            val createDb = assertNotNull(outCreateDb.value)

            val createCipherResult =
                sqlite3mc_config(createDb, SqliteMcConfig.CIPHER, None, SqliteMcCodecType.CHACHA20)

            assertEquals(SqliteMcCodecType.CHACHA20, createCipherResult)

            val keyResult = sqlite3_key(createDb, originalKey, originalKey.size)
            assertEquals(OK, keyResult)

            val createTableResult = sqlite3_exec(createDb, createSql, null, null, null)
            assertEquals(OK, createTableResult)

            val insertResult = sqlite3_exec(createDb, insertSql, null, null, null)
            assertEquals(OK, insertResult)

            // Control: the original key must be able to read back the row before any rekey.
            var beforeRekeyName: String? = null

            val beforeRekeyResult =
                sqlite3_exec(createDb, selectSql, null, null) { _, _, values, _ ->
                    beforeRekeyName = values[0]
                    0
                }

            assertEquals(OK, beforeRekeyResult)
            assertEquals("Mangue", beforeRekeyName)

            val rekeyResult = sqlite3_rekey(createDb, rotatedKey, rotatedKey.size)
            assertEquals(OK, rekeyResult)

            val cipherSalt =
                sqlite3mc_codec_data(createDb, null, SqliteMcCodecDataParam.CIPHER_SALT_RAW)

            assertNotNull(cipherSalt)
            assertEquals(16L, cipherSalt.byteSize)

            val closeCreateResult = sqlite3_close(createDb)
            assertEquals(OK, closeCreateResult)

            // The original key must no longer work once the database has been rekeyed.
            val outStaleDb = sqlite3.OutputParam()

            val staleOpenResult = sqlite3_open_v2(
                fileName = path,
                outDb = outStaleDb,
                flags = SqliteOpenFlag.READWRITE,
                vfs = null
            )
            assertEquals(OK, staleOpenResult)

            val staleDb = assertNotNull(outStaleDb.value)

            val staleCipherResult =
                sqlite3mc_config(staleDb, SqliteMcConfig.CIPHER, None, SqliteMcCodecType.CHACHA20)

            assertEquals(SqliteMcCodecType.CHACHA20, staleCipherResult)

            val staleKeyResult = sqlite3_key(staleDb, originalKey, originalKey.size)
            assertEquals(OK, staleKeyResult)

            var staleReadCallbackInvoked = false

            val staleReadResult = sqlite3_exec(staleDb, selectSql, null, null) { _, _, _, _ ->
                staleReadCallbackInvoked = true
                0
            }

            assertEquals(NOTADB, staleReadResult)
            assertFalse(staleReadCallbackInvoked)

            val closeStaleResult = sqlite3_close(staleDb)
            assertEquals(OK, closeStaleResult)

            // Reopen with the rotated key: reading must succeed and return the inserted row.
            val outReadDb = sqlite3.OutputParam()

            val readOpenResult = sqlite3_open_v2(
                fileName = path,
                outDb = outReadDb,
                flags = SqliteOpenFlag.READWRITE,
                vfs = null
            )
            assertEquals(OK, readOpenResult)

            val readDb = assertNotNull(outReadDb.value)

            val readCipherResult =
                sqlite3mc_config(readDb, SqliteMcConfig.CIPHER, None, SqliteMcCodecType.CHACHA20)

            assertEquals(SqliteMcCodecType.CHACHA20, readCipherResult)

            val readKeyResult = sqlite3_key(readDb, rotatedKey, rotatedKey.size)
            assertEquals(OK, readKeyResult)

            var selectedName: String? = null

            val readResult = sqlite3_exec(readDb, selectSql, null, null) { _, count, values, _ ->
                assertEquals(1, count)
                selectedName = values[0]
                0
            }

            assertEquals(OK, readResult)
            assertEquals("Mangue", selectedName)

            val closeReadResult = sqlite3_close(readDb)
            assertEquals(OK, closeReadResult)
        }
    }
}
