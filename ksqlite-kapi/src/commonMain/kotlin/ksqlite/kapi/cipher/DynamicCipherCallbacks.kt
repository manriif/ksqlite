package ksqlite.kapi.cipher

import ksqlite.capi.cipher.callbacks.CipherDescriptorAllocateCipherCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorCloneCipherCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorDecryptPageCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorEncryptPageCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorFreeCipherCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGenerateKeyCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetLegacyCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetPageSizeCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetReservedCallback
import ksqlite.capi.cipher.callbacks.CipherDescriptorGetSaltCallback
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.buffer.Buffer.Companion.wrap
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.helpers.ksqliteLog
import ksqlite.kapi.helpers.runCatchingSQLiteException
import ksqlite.kapi.sqliteRequireConnection
import ksqlite.types.SqliteResultCode

///////////////////////////////////////////////////////////////////////////
// Wrapper
///////////////////////////////////////////////////////////////////////////

/**
 * Wraps [cipher] keeping [factory] reachable for cloning.
 */
internal class DynamicCipherWrapper<Cipher : DynamicCipher>(
    private val factory: DynamicCipher.Factory<Cipher>,
    val cipher: Cipher
) {

    /**
     * Clones from [source] into this cipher.
     */
    @Suppress("UNCHECKED_CAST")
    fun clone(source: DynamicCipherWrapper<*>) =
        factory.clone((source as DynamicCipherWrapper<Cipher>).cipher, cipher)

    /**
     * Logs the error and returns the exception result code.
     */
    fun handleError(exception: SQLiteException): SqliteResultCode.Failure {
        ksqliteLog(exception.message, exception.result)
        return exception.result
    }

    /**
     * Executes [block] and returns [SqliteResultCode.OK] or an instance of [SqliteResultCode.Failure] if
     * [block]'s throws.
     */
    inline fun catching(block: Cipher.() -> Unit): SqliteResultCode.OkOrFailure {
        return cipher.runCatchingSQLiteException(::handleError) {
            block()
            SqliteResultCode.OK
        }
    }
}

/**
 * Returns a new [DynamicCipherWrapper].
 */
private fun <Cipher : DynamicCipher> DynamicCipher.Factory<Cipher>.create(
    scope: DynamicCipherCreateScope,
    connection: DatabaseConnection
): DynamicCipherWrapper<*> = DynamicCipherWrapper(this, scope.create(connection))

///////////////////////////////////////////////////////////////////////////
// Callbacks
///////////////////////////////////////////////////////////////////////////

private typealias Wrapper = DynamicCipherWrapper<*>

/**
 * Returns a [CipherDescriptorAllocateCipherCallback] that will create a [DynamicCipher] from
 * [factory].
 */
internal fun createCipherAllocateCipherCallback(
    cipherName: String,
    factory: DynamicCipher.Factory<*>
): CipherDescriptorAllocateCipherCallback<DynamicCipherWrapper<*>> =
    CipherDescriptorAllocateCipherCallback { db ->
        factory.runCatchingSQLiteException(
            handleException = { exception ->
                ksqliteLog(exception.message, exception.result)
                null
            },
            block = {
                DynamicCipherCreateScopeImpl(db, cipherName).use { scope ->
                    create(scope, sqliteRequireConnection(db))
                }
            }
        )
    }

internal val CipherFreeCipherCallback =
    CipherDescriptorFreeCipherCallback<Wrapper> { it.cipher.close() }

internal val CipherCloneCipherCallback =
    CipherDescriptorCloneCipherCallback(Wrapper::clone)

internal val CipherGetLegacyCallback =
    CipherDescriptorGetLegacyCallback<Wrapper> { if (it.cipher.isLegacy) 1 else 0 }

internal val CipherGetPageSizeCallback =
    CipherDescriptorGetPageSizeCallback<Wrapper> { it.cipher.pageSize }

internal val CipherGetReservedCallback =
    CipherDescriptorGetReservedCallback<Wrapper> { it.cipher.reserved }

internal val CipherGetSaltCallback =
    CipherDescriptorGetSaltCallback<Wrapper> { it.cipher.salt?.buffer }

internal val CipherGenerateKeyCallback =
    CipherDescriptorGenerateKeyCallback<Wrapper> { wrapper, userPassword, rekey, cipherSalt ->
        wrapper.cipher.generateKey(userPassword, rekey, cipherSalt?.wrap())
    }

internal val CipherEncryptPageCallback =
    CipherDescriptorEncryptPageCallback<Wrapper> { wrapper, page, data, reserved ->
        wrapper.catching { encryptPage(page, data.wrap(), reserved) }
    }

internal val CipherDecryptPageCallback =
    CipherDescriptorDecryptPageCallback<Wrapper> { wrapper, page, data, reserved, hmacCheck ->
        wrapper.catching { decryptPage(page, data.wrap(), reserved, hmacCheck != 0) }
    }