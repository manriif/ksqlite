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
import ksqlite.kapi.runSqliteTest
import ksqlite.kapi.runSqliteWalFileTest
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.cipher.SqliteMcCipher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests [CipherManager.register] and the [DynamicCipher] hooks.
 *
 * The cipher registered by these tests is a trivial, single-byte XOR "cipher" (see
 * [XorDynamicCipher]): it exists only to prove that every hook is wired up and invoked, not to
 * provide any real confidentiality.
 */
class DynamicCipherTest {

    @Test
    fun registrationWorks() = runSqliteTest { sqlite ->
        val cipherName = "kapi_xor_dynamic_registry"
        val tracking = XorDynamicCipherTracking()

        sqlite.ciphers.register(
            name = cipherName,
            factory = XorDynamicCipher.Factory(tracking),
            makeDefault = false
        )

        val cipher = SqliteMcCipher.Dynamic(cipherName)
        val index = sqlite.ciphers.getIndex(cipher)
        assertTrue(index > 0)
        assertEquals(cipherName, sqlite.ciphers.getName(index))
    }

    @Test
    fun registeringInvalidNameFails() = runSqliteTest { sqlite ->
        val tracking = XorDynamicCipherTracking()

        assertFailsWith<SQLiteException> {
            sqlite.ciphers.register("42-not-a-valid-start", XorDynamicCipher.Factory(tracking))
        }
    }

    @Test
    fun settingCipherDoesNotByItselfAllocateIt() = runSqliteTest { sqlite ->
        val cipherName = "kapi_xor_dynamic_lifecycle"
        val tracking = XorDynamicCipherTracking()

        sqlite.ciphers.register(cipherName, XorDynamicCipher.Factory(tracking), makeDefault = false)

        val connection = sqlite.open(":memory:")
        connection.cipherConfig.setCipher(cipherName, None)
        connection.close()

        // Associating the cipher with the connection via setCipher() alone, without ever
        // successfully deriving a key, does not trigger DynamicCipher.Factory.create(): the
        // allocate hook is deferred until key derivation actually happens.
        assertTrue(!tracking.created)
        assertTrue(!tracking.closed)
    }

    @Test
    fun dynamicCipherParametersWork() = runSqliteTest { sqlite ->
        val cipherName = "kapi_xor_dynamic_params"
        val tracking = XorDynamicCipherTracking()

        // The cipher must be registered before any connection is opened: a connection's codec
        // parameter table is cloned from the global one at open time.
        sqlite.ciphers.register(cipherName, XorDynamicCipher.Factory(tracking), makeDefault = false)

        val connection = sqlite.open(":memory:")
        connection.cipherConfig.setCipher(cipherName, None)

        val parameters = connection.cipherConfig.parameters(cipherName)
        assertEquals(42, parameters.get("test_param"))

        parameters.set("test_param", 7, None)
        assertEquals(7, parameters.get("test_param"))

        connection.close()
    }

    @Test
    fun createScopeCanReadConfiguredParameter() =
        runSqliteWalFileTest("kapi-dynamic-cipher-create-param.db") { sqlite, path ->
            val cipherName = "kapi_xor_dynamic_create_param"
            val tracking = XorDynamicCipherTracking()

            sqlite.ciphers.register(
                name = cipherName,
                factory = XorDynamicCipher.Factory(tracking),
                makeDefault = false
            )

            // Key derivation only actually engages the codec (rather than failing generically)
            // when the connection is opened through a cipher VFS -- see keyRekeyRoundTripWorks.
            val baseVfs = assertNotNull(sqlite.virtualFileSystems.default)
            val cipherVfs = sqlite.ciphers.virtualFileSystems.create(baseVfs, makeDefault = false)

            val connection = sqlite.open(
                fileName = path,
                flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.CREATE,
                vfs = cipherVfs.zName
            )

            connection.cipherConfig.setCipher(cipherName, None)

            val parameters = connection.cipherConfig.parameters(cipherName)

            parameters.set("test_param", 7, None)
            connection.setKey("kapi_xor_dynamic_create_param key".encodeToByteArray())

            assertEquals(7, tracking.parameterValueAtCreate)

            connection.close()
            sqlite.ciphers.virtualFileSystems.destroyAll()
        }

    @Test
    fun keyRekeyRoundTripWorks() = runSqliteWalFileTest("kapi-dynamic-cipher.db") { sqlite, path ->
        val cipherName = "kapi_xor_dynamic_roundtrip"
        val tracking = XorDynamicCipherTracking()

        sqlite.ciphers.register(cipherName, XorDynamicCipher.Factory(tracking), makeDefault = false)

        val baseVfs = assertNotNull(sqlite.virtualFileSystems.default)
        val cipherVfs = sqlite.ciphers.virtualFileSystems.create(baseVfs, makeDefault = false)

        val originalKey = "dynamic original passphrase".encodeToByteArray()
        val rotatedKey = "dynamic rotated passphrase".encodeToByteArray()

        val createConnection = sqlite.open(
            fileName = path,
            flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.CREATE,
            vfs = cipherVfs.zName
        )

        createConnection.cipherConfig.setCipher(cipherName, Default)
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

        // Control: the same connection must still read correctly right after rekey.
        var afterRekeyName: String? = null

        createConnection.execute("SELECT name FROM fruits WHERE id = 1;") { _, values, _ ->
            afterRekeyName = values[0]
            false
        }

        assertEquals("Mangue", afterRekeyName)

        val salt = assertNotNull(createConnection.cipherData.cipherSaltRaw())
        assertEquals(XOR_DYNAMIC_CIPHER_SALT_LENGTH, salt.byteSize)
        salt.close()

        createConnection.close()

        // The original key must no longer work once the database has been rekeyed.
        val staleConnection = sqlite.open(
            fileName = path,
            flags = SqliteOpenFlag.READWRITE,
            vfs = cipherVfs.zName
        )

        staleConnection.cipherConfig.setCipher(cipherName, None)
        staleConnection.setKey(originalKey)

        assertFailsWith<SQLiteException> {
            staleConnection.execute("SELECT name FROM fruits WHERE id = 1;")
        }

        staleConnection.close()

        // Reopen with the rotated key: reading must succeed and return the inserted row.
        val readConnection = sqlite.open(
            fileName = path,
            flags = SqliteOpenFlag.READWRITE,
            vfs = cipherVfs.zName
        )

        readConnection.cipherConfig.setCipher(cipherName, None)
        readConnection.setKey(rotatedKey)

        var selectedName: String? = null

        readConnection.execute("SELECT name FROM fruits WHERE id = 1;") { _, values, _ ->
            selectedName = values[0]
            false
        }

        assertEquals("Mangue", selectedName)

        readConnection.close()

        assertTrue(tracking.created)
        assertTrue(tracking.cloned)
        assertTrue(tracking.generateKeyCalled)
        assertTrue(tracking.encryptPageCalled)
        assertTrue(tracking.decryptPageCalled)

        sqlite.ciphers.virtualFileSystems.destroyAll()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed SQLite instance violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun registerFailsOnceSqliteInstanceClosed() = runSqliteTest { sqlite ->
        sqlite.close()

        assertFailsWith<IllegalStateException> {
            sqlite.ciphers.register(
                "kapi_xor_dynamic_closed",
                XorDynamicCipher.Factory(XorDynamicCipherTracking())
            )
        }
    }
}
