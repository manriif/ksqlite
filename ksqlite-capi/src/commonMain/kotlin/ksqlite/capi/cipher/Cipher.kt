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
@file:OptIn(ExperimentalAtomicApi::class)
@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.cipher

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
import ksqlite.capi.memory.Buffer
import ksqlite.capi.sqlite3
import ksqlite.internal.runtime.concurrency.SafeLock
import ksqlite.internal.runtime.concurrency.withLock
import ksqlite.types.SqliteResultCode
import ksqlite.types.internal.convertResultCode
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Callbacks of a Cipher Descriptor.
 */
internal class CipherCallbacks<Cipher : Any>(
    val saltSize: Long,
    val allocate: CipherDescriptorAllocateCipherCallback<Cipher>,
    val free: CipherDescriptorFreeCipherCallback<Cipher>,
    val clone: CipherDescriptorCloneCipherCallback<Cipher>,
    val getLegacy: CipherDescriptorGetLegacyCallback<Cipher>,
    val getPageSize: CipherDescriptorGetPageSizeCallback<Cipher>,
    val getReserved: CipherDescriptorGetReservedCallback<Cipher>,
    val getSalt: CipherDescriptorGetSaltCallback<Cipher>,
    val generateKey: CipherDescriptorGenerateKeyCallback<Cipher>,
    val encryptPage: CipherDescriptorEncryptPageCallback<Cipher>,
    val decryptPage: CipherDescriptorDecryptPageCallback<Cipher>
) {

    /**
     * Invokes the [CipherCallbacks.allocate].
     */
    inline fun allocate(db: sqlite3): CipherWrapper<Cipher>? =
        allocate.apply(db)?.let { CipherWrapper(this, it) }
}

/**
 * Holder and invoker for [CipherDescriptor] callbacks.
 */
internal class CipherWrapper<Cipher : Any>(
    private val callbacks: CipherCallbacks<Cipher>,
    private val cipher: Cipher
) {

    val saltSize: Long
        get() = callbacks.saltSize

    /**
     * Invokes the [CipherCallbacks.free].
     */
    inline fun free() = callbacks.free.apply(cipher)

    /**
     * Invokes the [CipherCallbacks.clone].
     */
    @Suppress("UNCHECKED_CAST")
    inline fun clone(from: CipherWrapper<*>) = callbacks.clone.apply(cipher, from.cipher as Cipher)

    /**
     * Invokes the [CipherCallbacks.getLegacy].
     */
    inline fun getLegacy() = callbacks.getLegacy.apply(cipher)

    /**
     * Invokes the [CipherCallbacks.getPageSize].
     */
    inline fun getPageSize() = callbacks.getPageSize.apply(cipher)

    /**
     * Invokes the [CipherCallbacks.getReserved].
     */
    inline fun getReserved() = callbacks.getReserved.apply(cipher)

    /**
     * Invokes the [CipherCallbacks.getSalt].
     */
    inline fun getSalt() = callbacks.getSalt.apply(cipher)

    /**
     * Invokes the [CipherCallbacks.generateKey].
     */
    inline fun generateKey(
        userPassword: ByteArray,
        rekey: Int,
        cipherSalt: Buffer?
    ) = callbacks.generateKey.apply(cipher, userPassword, rekey, cipherSalt)

    /**
     * Invokes the [CipherCallbacks.encryptPage].
     */
    inline fun encryptPage(
        page: Int,
        data: Buffer,
        reserved: Int
    ) = callbacks.encryptPage.apply(cipher, page, data, reserved).code

    /**
     * Invokes the [CipherCallbacks.decryptPage].
     */
    inline fun decryptPage(
        page: Int,
        data: Buffer,
        reserved: Int,
        hmacCheck: Int
    ) = callbacks.decryptPage.apply(cipher, page, data, reserved, hmacCheck).code
}

///////////////////////////////////////////////////////////////////////////
// Slots
///////////////////////////////////////////////////////////////////////////

private const val DYNAMIC_CIPHER_MAX = 4

private val DynamicCipherLock = SafeLock()
private var DynamicCipherCount = 0
private val DynamicCipherCallbacks = Array<CipherCallbacks<*>?>(DYNAMIC_CIPHER_MAX) { null }

/**
 * Returns an array of allocators [A], ensuring it contains the expected number of entries.
 */
internal inline fun <reified A : Any> createCipherAllocators(vararg allocators: A): Array<A> {
    check(allocators.size == DYNAMIC_CIPHER_MAX)
    return arrayOf(*allocators)
}

/**
 * Registers the cipher identified by [callbacks].
 *
 * @throws UnsupportedOperationException if the number of registered ciphers exceed the maximum
 * allowed ciphers.
 */
internal fun <Allocator : Any> registerCipher(
    callbacks: CipherCallbacks<*>,
    allocators: Array<Allocator>,
    setAllocator: (Allocator?) -> Unit,
    register: () -> Int
): SqliteResultCode = DynamicCipherLock.withLock {
    val index = DynamicCipherCount

    check(index < DYNAMIC_CIPHER_MAX) {
        "No more available slot for dynamic cipher"
    }

    val allocator = checkNotNull(allocators.getOrNull(index)) {
        "Invalid number of allocators (${allocators.size} vs $DYNAMIC_CIPHER_MAX)"
    }

    setAllocator(allocator)
    val result = convertResultCode(register())

    if (result == SqliteResultCode.OK) {
        DynamicCipherCallbacks[index] = callbacks
        DynamicCipherCount++
    } else {
        setAllocator(null)
    }

    result
}

/**
 * Unregisters all the registered ciphers.
 */
internal fun unregisterCiphers() = DynamicCipherLock.withLock {
    repeat(DynamicCipherCallbacks.size) { index ->
        DynamicCipherCallbacks[index] = null
    }

    DynamicCipherCount = 0
}

///////////////////////////////////////////////////////////////////////////
// Callbacks
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates a new cipher for the [CipherCallbacks] at [index] and returns a [CipherWrapper].
 */
internal fun cipherAllocate(
    db: sqlite3,
    index: Int
): CipherWrapper<*>? = checkNotNull(DynamicCipherCallbacks[index]) {
    "No cipher callbacks found for index $index"
}.allocate(db)