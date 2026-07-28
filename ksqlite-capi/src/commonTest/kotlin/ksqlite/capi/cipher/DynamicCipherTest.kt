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
import ksqlite.capi.sqlite3mc_register_cipher
import ksqlite.capi.usingRealTempFile
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.cipher.SqliteMcCipher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the SQLite3 Multiple Ciphers dynamic cipher registration API.
 *
 * The cipher registered by these tests is a trivial, single-byte XOR "cipher": it exists only to
 * prove that every hook of [CipherDescriptor] is actually wired up and invoked by SQLite3 Multiple
 * Ciphers, not to provide any real confidentiality.
 */
class DynamicCipherTest {

    @Test
    fun registrationWorks() = runSqliteTest {
        val cipherName = "xordynamicregistry"
        val tracking = XorCipherCallbackTracking()
        val descriptor = xorCipherDescriptor(cipherName, tracking)
        val params = allocateTestParamArray()

        val cipherCountBefore = sqlite3mc_cipher_count()

        val registerResult = sqlite3mc_register_cipher(descriptor, params, 0)
        assertEquals(OK, registerResult)

        val testParam = params[0]
        assertEquals("test_param", testParam.m_name)
        assertEquals(42, testParam.m_value)

        val cipherCountAfter = sqlite3mc_cipher_count()
        assertEquals(cipherCountBefore + 1, cipherCountAfter)

        val cipher = SqliteMcCipher.Dynamic(cipherName)
        val cipherIndex = sqlite3mc_cipher_index(cipher)
        assertTrue(cipherIndex > 0)

        val registeredName = sqlite3mc_cipher_name(cipherIndex)
        assertEquals(cipherName, registeredName)

        params.close()
        descriptor.close()
    }

    @Test
    fun configCipherWorks() = runSqliteTest {
        val cipherName = "xordynamicconfig"
        val tracking = XorCipherCallbackTracking()
        val descriptor = xorCipherDescriptor(cipherName, tracking)
        val params = allocateTestParamArray()

        // Register the cipher before opening any connection: a connection's codec parameter table
        // is cloned from the global one at open time, so a connection opened beforehand would never
        // learn about this cipher's custom parameters.
        val registerResult = sqlite3mc_register_cipher(descriptor, params, 0)
        assertEquals(OK, registerResult)

        val outDb = sqlite3.OutputParam()

        val openResult = sqlite3_open_v2(
            fileName = "xordynamicconfig_test",
            outDb = outDb,
            flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
            vfs = null
        )
        assertEquals(OK, openResult)

        val db = assertNotNull(outDb.value)

        val cipher = SqliteMcCipher.Dynamic(cipherName)
        val param = SqliteMcCipher.Dynamic.Parameter("test_param")

        val initialValue = sqlite3mc_config_cipher(db, cipher, param, None)
        assertEquals(42, initialValue)

        val newValue = 7

        val updatedValue = sqlite3mc_config_cipher(db, cipher, param, None, newValue)
        assertEquals(newValue, updatedValue)

        val closeResult = sqlite3_close(db)
        assertEquals(OK, closeResult)

        params.close()
        descriptor.close()
    }

    @Test
    fun keyRekeyRoundTripWorks() = runSqliteTest {
        val cipherName = "xordynamicroundtrip"
        val tracking = XorCipherCallbackTracking()
        val descriptor = xorCipherDescriptor(cipherName, tracking)
        val params = allocateSentinelOnlyParamArray()

        val registerResult = sqlite3mc_register_cipher(descriptor, params, 0)
        assertEquals(OK, registerResult)

        val cipher = SqliteMcCipher.Dynamic(cipherName)

        findVfs().usingRealTempFile("dynamic-cipher.db") { path ->
            val originalKey = "dynamic original passphrase".encodeToByteArray()
            val rotatedKey = "dynamic rotated passphrase".encodeToByteArray()

            val createSql = "CREATE TABLE fruits(id INTEGER, name TEXT);"
            val insertSql = "INSERT INTO fruits VALUES (1, 'Mangue');"
            val selectSql = "SELECT name FROM fruits WHERE id = 1;"

            // Create the encrypted database using the dynamically registered cipher.
            val outCreateDb = sqlite3.OutputParam()

            val createOpenResult = sqlite3_open_v2(
                fileName = path,
                outDb = outCreateDb,
                flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.CREATE,
                vfs = null
            )
            assertEquals(OK, createOpenResult)

            val createDb = assertNotNull(outCreateDb.value)

            val createCipherResult = sqlite3mc_config(
                db = createDb,
                param = SqliteMcConfig.CIPHER,
                prefix = Default,
                newValue = cipher
            )
            assertEquals(cipher, createCipherResult)

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

            // Control: the same connection must still read correctly right after rekey.
            var afterRekeyName: String? = null

            val afterRekeyResult =
                sqlite3_exec(createDb, selectSql, null, null) { _, _, values, _ ->
                    afterRekeyName = values[0]
                    0
                }

            assertEquals(OK, afterRekeyResult)
            assertEquals("Mangue", afterRekeyName)

            val cipherSalt =
                sqlite3mc_codec_data(createDb, null, SqliteMcCodecDataParam.CIPHER_SALT_RAW)

            assertNotNull(cipherSalt)
            assertEquals(XOR_CIPHER_SALT_LENGTH, cipherSalt.byteSize)

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

            val staleCipherResult = sqlite3mc_config(staleDb, SqliteMcConfig.CIPHER, None, cipher)
            assertEquals(cipher, staleCipherResult)

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

            val readCipherResult = sqlite3mc_config(readDb, SqliteMcConfig.CIPHER, None, cipher)
            assertEquals(cipher, readCipherResult)

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

        assertTrue(tracking.allocateCalled)
        assertTrue(tracking.freeCalled)
        assertTrue(tracking.cloneCalled)
        assertTrue(tracking.getLegacyCalled)
        assertTrue(tracking.getPageSizeCalled)
        assertTrue(tracking.getReservedCalled)
        assertTrue(tracking.getSaltCalled)
        assertTrue(tracking.generateKeyCalled)
        assertTrue(tracking.encryptPageCalled)
        assertTrue(tracking.decryptPageCalled)

        params.close()
        descriptor.close()
    }
}
