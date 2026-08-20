package ksqlite.kapi.cipher

import ksqlite.capi.cipher.CipherDescriptor
import ksqlite.capi.cipher.CipherParams
import ksqlite.capi.memory.StructArray
import ksqlite.capi.memory.allocateArray
import ksqlite.capi.sqlite3mc_cipher_count
import ksqlite.capi.sqlite3mc_cipher_index
import ksqlite.capi.sqlite3mc_cipher_name
import ksqlite.capi.sqlite3mc_register_cipher
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteOutOfMemoryCheck
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.throwSQLiteException
import ksqlite.types.cipher.SqliteMcCipher

/**
 * Note that this instance must be closed to release registered ciphers.
 */
internal class CipherManagerImpl(private val scope: CloseableScope) : CipherManager, AutoCloseable {

    private val registeredCiphers = mutableListOf<CipherDescriptor>()

    override val config = CipherConfigurationImpl(null, scope)
    override val virtualFileSystems = CipherVirtualFileSystemManagerImpl(scope)

    override val count: Int
        get() = scope.notClosed { sqlite3mc_cipher_count() }

    override fun getIndex(cipher: SqliteMcCipher): Int = scope.notClosed {
        sqlite3mc_cipher_index(cipher).takeIf { it != -1 }
            ?: throwSQLiteException("Cipher ${cipher.name} is not registered")
    }

    override fun getName(index: Int): String = scope.notClosed {
        sqlite3mc_cipher_name(index)
            ?: throwSQLiteException("No cipher exists for index $index")
    }

    /**
     * Allocates the cipher parameters for this factory.
     */
    private fun DynamicCipher.Factory<*>.allocateParameters(): StructArray<CipherParams> {
        val callbacks = mutableListOf<DynamicCipherParameter.() -> Unit>()

        DynamicCipherParameterRegistryImpl(callbacks).use { it.registerParameters() }

        val params = CipherParams.allocateArray(callbacks.size + 1) { index ->
            m_name = ""

            if (index < callbacks.size) {
                callbacks[index].invoke(this)

                ensureValidCipherName(m_name) {
                    "For param at index ${index}: name $m_name is not a valid cipher name"
                }
            }
        }

        return sqliteOutOfMemoryCheck(params) {
            "Failed to allocate cipher parameters"
        }
    }

    override fun <C : DynamicCipher> register(
        name: String,
        factory: DynamicCipher.Factory<C>,
        makeDefault: Boolean
    ): Unit = scope.notClosed {
        ensureValidCipherName(name) { "Name $name is not a valid cipher name" }

        val descriptor = CipherDescriptor(
            name = name,
            saltSize = factory.saltSize,
            allocate = createCipherAllocateCipherCallback(name, factory),
            free = CipherFreeCipherCallback,
            clone = CipherCloneCipherCallback,
            getLegacy = CipherGetLegacyCallback,
            getPageSize = CipherGetPageSizeCallback,
            getReserved = CipherGetReservedCallback,
            getSalt = CipherGetSaltCallback,
            generateKey = CipherGenerateKeyCallback,
            encryptPage = CipherEncryptPageCallback,
            decryptPage = CipherDecryptPageCallback,
        )

        val params = factory.runCatching { allocateParameters() }.getOrElse { cause ->
            descriptor.close()
            throw cause
        }

        val result = sqlite3mc_register_cipher(
            descriptor = descriptor,
            params = params,
            makeDefault = if (makeDefault) 1 else 0
        )

        params.close()
        sqliteResultCheck(result, cleanup = descriptor::close)
        registeredCiphers.add(descriptor)
    }

    override fun close() {
        registeredCiphers
            .onEach(AutoCloseable::close)
            .clear()
    }
}