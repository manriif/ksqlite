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
@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.cipher

import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.StaticMemoryAllocator
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.readBytes
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.sqlite3
import ksqlite.foreign.AllocateCipher_t
import ksqlite.foreign.CloneCipher_t
import ksqlite.foreign.DecryptPage_t
import ksqlite.foreign.EncryptPage_t
import ksqlite.foreign.FreeCipher_t
import ksqlite.foreign.GenerateKey_t
import ksqlite.foreign.GetLegacy_t
import ksqlite.foreign.GetPageSize_t
import ksqlite.foreign.GetReserved_t
import ksqlite.foreign.GetSalt_t
import java.lang.foreign.MemorySegment

private val CipherMemoryManager = MemoryManager()

/**
 * Returns a native cipher allocator function operating on the cipher at [index].
 */
private fun cipherAllocator(index: Int): MemorySegment = AllocateCipher_t.allocate({ db ->
    cipherAllocate(sqlite3(db), index)?.let { wrapper ->
        CipherMemoryManager.stableRefPointer(wrapper, null, null)
    }
}, StaticMemoryAllocator)

internal val CipherAllocateCipherHandlers = createCipherAllocators(
    cipherAllocator(0),
    cipherAllocator(1),
    cipherAllocator(2),
    cipherAllocator(3)
)

internal val CipherFreeCipherHandler = FreeCipher_t.allocate({ cipher ->
    CipherMemoryManager.getStableRef<Nothing>(cipher).let { reference ->
        (reference.data as CipherWrapper<*>).free().also {
            reference.dispose()
        }
    }
}, StaticMemoryAllocator)

/**
 * Returns the [CipherWrapper] backed by this opaque pointer.
 */
private inline fun MemorySegment.getCipherWrapper(): CipherWrapper<*> =
    CipherMemoryManager.stableRefData(this)

internal val CipherCloneCipherHandler = CloneCipher_t.allocate({ cipherTo, cipherFrom ->
    cipherTo.getCipherWrapper().clone(cipherFrom.getCipherWrapper())
}, StaticMemoryAllocator)

internal val CipherGetLegacyHandler = GetLegacy_t.allocate({ cipher ->
    cipher.getCipherWrapper().getLegacy()
}, StaticMemoryAllocator)

internal val CipherGetPageSizeHandler = GetPageSize_t.allocate({ cipher ->
    cipher.getCipherWrapper().getPageSize()
}, StaticMemoryAllocator)

internal val CipherGetReservedHandler = GetReserved_t.allocate({ cipher ->
    cipher.getCipherWrapper().getReserved()
}, StaticMemoryAllocator)

internal val CipherGetSaltHandler = GetSalt_t.allocate({ cipher ->
    cipher.getCipherWrapper().getSalt()?.pointer.notNull
}, StaticMemoryAllocator)

internal val CipherGenerateKeyHandler =
    GenerateKey_t.allocate({ cipher, userPassword, passwordLength, rekey, cipherSalt ->
        cipher.getCipherWrapper().run {
            generateKey(
                userPassword = userPassword.readBytes(passwordLength),
                rekey = rekey,
                cipherSalt = Buffer.from(cipherSalt, saltSize)
            )
        }
    }, StaticMemoryAllocator)

internal val CipherEncryptPageHandler =
    EncryptPage_t.allocate({ cipher, page, data, len, reserved ->
        cipher.getCipherWrapper().encryptPage(
            page = page,
            data = Buffer.from(data, len.toLong())!!,
            reserved = reserved
        )
    }, StaticMemoryAllocator)

internal val CipherDecryptPageHandler =
    DecryptPage_t.allocate({ cipher, page, data, len, reserved, hmacCheck ->
        cipher.getCipherWrapper().decryptPage(
            page = page,
            data = Buffer.from(data, len.toLong())!!,
            reserved = reserved,
            hmacCheck = hmacCheck
        )
    }, StaticMemoryAllocator)