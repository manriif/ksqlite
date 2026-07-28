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

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.memory.Buffer
import ksqlite.capi.s3
import ksqlite.capi.sqlite3

/**
 * Invokes the cipher allocator at [index].
 */
private fun cipherAllocate(
    db: CPointer<s3>?,
    index: Int
): COpaquePointer? = cipherAllocate(sqlite3(db!!), index)?.let { wrapper ->
    StableRef.create(wrapper).asCPointer()
}

internal val CipherAllocateCipherHandlers = createCipherAllocators(
    staticCFunction { db: CPointer<s3>? -> cipherAllocate(db, 0) },
    staticCFunction { db: CPointer<s3>? -> cipherAllocate(db, 1) },
    staticCFunction { db: CPointer<s3>? -> cipherAllocate(db, 2) },
    staticCFunction { db: CPointer<s3>? -> cipherAllocate(db, 3) },
)

/**
 * Returns the [StableRef] to [CipherWrapper] backed by this opaque pointer.
 */
private inline fun COpaquePointer?.getCipherWrapperRef(): StableRef<CipherWrapper<*>> =
    this!!.asStableRef<CipherWrapper<*>>()

/**
 * Returns the [CipherWrapper] backed by this opaque pointer.
 */
private inline fun COpaquePointer?.getCipherWrapper(): CipherWrapper<*> =
    getCipherWrapperRef().get()

internal val CipherFreeCipherHandler = staticCFunction { cipher: COpaquePointer? ->
    cipher.getCipherWrapperRef()
        .apply { get().free() }
        .dispose()
}

internal val CipherCloneCipherHandler = staticCFunction { cipherTo: COpaquePointer?,
                                                          cipherFrom: COpaquePointer? ->
    cipherTo.getCipherWrapper().clone(cipherFrom.getCipherWrapper())
}

internal val CipherGetLegacyHandler = staticCFunction { cipher: COpaquePointer? ->
    cipher.getCipherWrapper().getLegacy()
}

internal val CipherGetPageSizeHandler = staticCFunction { cipher: COpaquePointer? ->
    cipher.getCipherWrapper().getPageSize()
}

internal val CipherGetReservedHandler = staticCFunction { cipher: COpaquePointer? ->
    cipher.getCipherWrapper().getReserved()
}

internal val CipherGetSaltHandler = staticCFunction { cipher: COpaquePointer? ->
    cipher.getCipherWrapper().getSalt()?.pointer?.reinterpret<UByteVar>()
}

internal val CipherGenerateKeyHandler = staticCFunction { cipher: COpaquePointer?,
                                                          userPassword: CPointer<ByteVar>?,
                                                          passwordLength: Int,
                                                          rekey: Int,
                                                          cipherSalt: CPointer<UByteVar>? ->
    cipher.getCipherWrapper().run {
        generateKey(
            userPassword = userPassword!!.readBytes(passwordLength),
            rekey = rekey,
            cipherSalt = Buffer.from(cipherSalt, saltSize)
        )
    }
}

internal val CipherEncryptPageHandler = staticCFunction { cipher: COpaquePointer?,
                                                          page: Int,
                                                          data: CPointer<UByteVar>?,
                                                          len: Int,
                                                          reserved: Int ->
    cipher.getCipherWrapper().encryptPage(
        page = page,
        data = Buffer.from(data, len.toLong())!!,
        reserved = reserved
    )
}

internal val CipherDecryptPageHandler = staticCFunction { cipher: COpaquePointer?,
                                                          page: Int,
                                                          data: CPointer<UByteVar>?,
                                                          len: Int,
                                                          reserved: Int,
                                                          hmacCheck: Int ->
    cipher.getCipherWrapper().decryptPage(
        page = page,
        data = Buffer.from(data, len.toLong())!!,
        reserved = reserved,
        hmacCheck = hmacCheck
    )
}