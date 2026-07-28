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

import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.StructArray
import ksqlite.capi.memory.allocateArray
import ksqlite.capi.memory.readBytes
import ksqlite.capi.sqlite3_free
import ksqlite.capi.sqlite3_malloc
import ksqlite.capi.sqlite3_randomness
import kotlin.test.assertNotNull

internal const val XOR_CIPHER_SALT_LENGTH = 16L

/**
 * Tracks which [CipherDescriptor] callbacks have actually been invoked by SQLite3 Multiple
 * Ciphers for a given registered cipher.
 */
internal class XorCipherCallbackTracking {
    var allocateCalled = false
    var freeCalled = false
    var cloneCalled = false
    var getLegacyCalled = false
    var getPageSizeCalled = false
    var getReservedCalled = false
    var getSaltCalled = false
    var generateKeyCalled = false
    var encryptPageCalled = false
    var decryptPageCalled = false
}

/**
 * Per-connection state of the XOR cipher. Holds a single derived key byte, plus the salt buffer
 * whose content is reported to SQLite3 Multiple Ciphers through [CipherDescriptor]'s getSalt
 * hook so that it can be persisted in (and later read back from) the database header.
 */
private class XorCipherState(val saltBuffer: Buffer) {
    var keyByte: Byte = 0
}

private fun xored(bytes: ByteArray, keyByte: Byte): ByteArray {
    for (index in bytes.indices) {
        bytes[index] = (bytes[index].toInt() xor keyByte.toInt()).toByte()
    }

    return bytes
}

/**
 * Builds a [CipherDescriptor] for a trivial XOR cipher named [name], recording every callback
 * invocation into [tracking].
 *
 * SQLite3 Multiple Ciphers' own correctness isn't under test here — only that every hook of
 * [CipherDescriptor] is actually wired up and invoked, not to provide any real confidentiality.
 */
internal fun xorCipherDescriptor(
    name: String,
    tracking: XorCipherCallbackTracking
): CipherDescriptor = CipherDescriptor(
    name = name,
    saltSize = XOR_CIPHER_SALT_LENGTH,
    allocate = {
        tracking.allocateCalled = true
        sqlite3_malloc(XOR_CIPHER_SALT_LENGTH.toInt())?.let(::XorCipherState)
    },
    free = { cipher ->
        tracking.freeCalled = true
        sqlite3_free(cipher.saltBuffer)
    },
    clone = { cipherTo, cipherFrom ->
        tracking.cloneCalled = true
        cipherTo.keyByte = cipherFrom.keyByte
        cipherTo.saltBuffer.write(cipherFrom.saltBuffer.readBytes())
    },
    getLegacy = {
        tracking.getLegacyCalled = true
        0
    },
    getPageSize = {
        tracking.getPageSizeCalled = true
        0
    },
    getReserved = {
        tracking.getReservedCalled = true
        0
    },
    getSalt = { cipher ->
        tracking.getSaltCalled = true
        cipher.saltBuffer
    },
    generateKey = { cipher, userPassword, _, cipherSalt ->
        tracking.generateKeyCalled = true

        if (cipherSalt != null) {
            cipher.saltBuffer.write(cipherSalt.readBytes())
        } else {
            sqlite3_randomness(XOR_CIPHER_SALT_LENGTH.toInt(), cipher.saltBuffer)
        }

        var derivedKeyByte = 0
        for (byte in userPassword) derivedKeyByte = derivedKeyByte xor byte.toInt()

        cipher.keyByte = derivedKeyByte.toByte()
    },
    encryptPage = { cipher, _, data, _ ->
        tracking.encryptPageCalled = true
        data.write(xored(data.readBytes(), cipher.keyByte))
        OK
    },
    decryptPage = { cipher, _, data, _, _ ->
        tracking.decryptPageCalled = true
        data.write(xored(data.readBytes(), cipher.keyByte))
        OK
    }
)

/**
 * Allocates a contiguous [CipherParams] array holding a single custom `test_param` entry
 * followed by the mandatory sentinel (empty-name) entry terminating the list.
 */
internal fun allocateTestParamArray(): StructArray<CipherParams> = assertNotNull(
    CipherParams.allocateArray(2) { index ->
        when (index) {
            0 -> {
                m_name = "test_param"
                m_value = 42
                m_default = 42
                m_minValue = 0
                m_maxValue = 100
            }
            else -> {
                m_name = ""
                m_value = 0
                m_default = 0
                m_minValue = 0
                m_maxValue = 0
            }
        }
    }
)

/**
 * Allocates a contiguous [CipherParams] array holding only the mandatory sentinel
 * (empty-name) entry, for ciphers that expose no custom configuration parameters.
 */
internal fun allocateSentinelOnlyParamArray(): StructArray<CipherParams> = assertNotNull(
    CipherParams.allocateArray(1) { _ ->
        m_name = ""
        m_value = 0
        m_default = 0
        m_minValue = 0
        m_maxValue = 0
    }
)
