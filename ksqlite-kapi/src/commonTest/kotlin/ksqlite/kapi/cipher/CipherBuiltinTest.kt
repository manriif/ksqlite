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

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.kapi.runSqliteTest
import ksqlite.kapi.runSqliteWalFileTest
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcCodecType
import ksqlite.types.cipher.SqliteMcConfigParamPrefix
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the SQLite Multiple Ciphers builtin cipher APIs: [CipherManager], [CipherConfiguration],
 * [CipherParameters] and [CipherData].
 */
class CipherBuiltinTest {

    @Test
    fun cipherRegistryWorks() = runSqliteTest { sqlite ->
        assertTrue(sqlite.ciphers.count >= 6)

        val chacha20Index = sqlite.ciphers.getIndex(SqliteMcCodecType.CHACHA20)
        assertTrue(chacha20Index > 0)
        assertEquals("chacha20", sqlite.ciphers.getName(chacha20Index))

        val aes256Index = sqlite.ciphers.getIndex(SqliteMcCodecType.AES256)
        assertTrue(aes256Index > 0)
        assertEquals("aes256cbc", sqlite.ciphers.getName(aes256Index))

        assertFailsWith<SQLiteException> {
            sqlite.ciphers.getIndex(SqliteMcCipher.Dynamic("doesNotExist"))
        }

        assertFailsWith<SQLiteException> {
            sqlite.ciphers.getName(sqlite.ciphers.count + 1)
        }
    }

    @Test
    fun globalConfigIsReadOnlyWithoutConnection() = runSqliteTest { sqlite ->
        val config = sqlite.ciphers.config

        assertEquals(SqliteMcCodecType.CHACHA20, config.getCipher())

        assertFalse(config.isHmacCheckEnabled(SqliteMcConfigParamPrefix.Min))
        assertTrue(config.isHmacCheckEnabled(SqliteMcConfigParamPrefix.Max))
    }

    @Test
    fun perConnectionConfigWorks() = runSqliteConnectionTest { _, connection ->
        val config = connection.cipherConfig

        assertTrue(config.isHmacCheckEnabled)
        config.setHmacCheckEnabled(false)
        assertFalse(config.isHmacCheckEnabled)

        val legacyWalEnabledBefore = config.isLegacyWalEnabled
        config.setLegacyWalEnabled(!legacyWalEnabledBefore)
        assertEquals(!legacyWalEnabledBefore, config.isLegacyWalEnabled)

        val parameters = config.parameters(SqliteMcCodecType.CHACHA20)
        val kdfIter = parameters[SqliteMcCodecType.CHACHA20.KDF_ITER]
        assertTrue(kdfIter > 0)

        val newKdfIter = 32_000
        parameters[SqliteMcCodecType.CHACHA20.KDF_ITER] = newKdfIter
        assertEquals(newKdfIter, parameters[SqliteMcCodecType.CHACHA20.KDF_ITER])
    }

    @Test
    fun keyRekeyRoundTripWorks() = runSqliteWalFileTest("kapi-cipher.db") { sqlite, path ->
        val baseVfs = assertNotNull(sqlite.virtualFileSystems.default)
        val cipherVfs = sqlite.ciphers.virtualFileSystems.create(baseVfs, makeDefault = false)

        val cipherVfsName = cipherVfs.zName
        val originalKey = "correct horse battery staple".encodeToByteArray()
        val rotatedKey = "new rotated passphrase".encodeToByteArray()

        val createConnection = sqlite.open(
            fileName = path,
            flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.CREATE,
            vfs = cipherVfsName
        )

        createConnection.cipherConfig.setCipher(
            SqliteMcCodecType.CHACHA20,
            SqliteMcConfigParamPrefix.None
        )
        createConnection.setKey(originalKey)

        createConnection.execute("CREATE TABLE fruits(id INTEGER, name TEXT);")
        createConnection.execute("INSERT INTO fruits VALUES (1, 'Mangue');")

        // Control: the original key must be able to read back the row before any rekey.
        var beforeRekeyName: String? = null

        createConnection.execute("SELECT name FROM fruits WHERE id = 1;") { _, values, _ ->
            beforeRekeyName = values[0]
            false
        }

        assertEquals("Mangue", beforeRekeyName)

        createConnection.setReKey(rotatedKey)

        val salt = assertNotNull(createConnection.cipherData.cipherSaltRaw())
        assertEquals(16L, salt.byteSize)
        salt.close()

        createConnection.close()

        // The original key must no longer work once the database has been rekeyed.
        val staleConnection = sqlite.open(
            fileName = path,
            flags = SqliteOpenFlag.READWRITE,
            vfs = cipherVfsName
        )

        staleConnection.cipherConfig.setCipher(
            SqliteMcCodecType.CHACHA20,
            SqliteMcConfigParamPrefix.None
        )
        staleConnection.setKey(originalKey)

        assertFailsWith<SQLiteException> {
            staleConnection.execute("SELECT name FROM fruits WHERE id = 1;")
        }

        staleConnection.close()

        // Reopen with the rotated key: reading must succeed and return the inserted row.
        val readConnection = sqlite.open(
            fileName = path,
            flags = SqliteOpenFlag.READWRITE,
            vfs = cipherVfsName
        )

        readConnection.cipherConfig.setCipher(
            SqliteMcCodecType.CHACHA20,
            SqliteMcConfigParamPrefix.None
        )
        readConnection.setKey(rotatedKey)

        var selectedName: String? = null

        readConnection.execute("SELECT name FROM fruits WHERE id = 1;") { _, values, _ ->
            selectedName = values[0]
            false
        }

        assertEquals("Mangue", selectedName)

        readConnection.close()
        sqlite.ciphers.virtualFileSystems.destroyAll()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun connectionScopedOperationsFailOnceConnectionClosed() =
        runSqliteConnectionTest { _, connection ->
            val cipherConfig = connection.cipherConfig
            val cipherData = connection.cipherData
            val parameters = cipherConfig.parameters(SqliteMcCodecType.CHACHA20)
            connection.close()

            assertFailsWith<IllegalStateException> { cipherConfig.getCipher() }
            assertFailsWith<IllegalStateException> { cipherData.cipherSaltRaw() }
            assertFailsWith<IllegalStateException> {
                parameters[SqliteMcCodecType.CHACHA20.KDF_ITER]
            }
        }

    @Test
    fun managerOperationsFailOnceSqliteClosed() = runSqliteTest { sqlite ->
        val manager = sqlite.ciphers
        sqlite.close()

        assertFailsWith<IllegalStateException> { manager.count }
        assertFailsWith<IllegalStateException> { manager.getIndex(SqliteMcCodecType.CHACHA20) }
        assertFailsWith<IllegalStateException> { manager.config.getCipher() }
    }
}
