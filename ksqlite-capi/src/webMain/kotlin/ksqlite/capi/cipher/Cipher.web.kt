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
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.readBytes
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.sqlite3
import ksqlite.capi.wasm
import ksqlite.foreign.wasm.FunctionSignature.Void
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.foreign.wasm.FunctionSignature.Int32 as I32
import ksqlite.foreign.wasm.FunctionSignature.Pointer as Ptr

private val CipherMemoryManager = MemoryManager()

@JsFun("(index, handler) => (p0) => handler(p0, index)")
private external fun cipherAllocateCipher(
    index: Int,
    handler: (db: WasmPointer, index: Int) -> WasmPointer?
): JsFunction

/**
 * Returns a pointer to a JS function allocating operating on the cipher at [index].
 */
private fun cipherAllocator(index: Int): WasmPointer = wasm.installFunction(
    signature = Ptr(Ptr),
    function = cipherAllocateCipher(index) { db, index ->
        cipherAllocate(sqlite3(db), index)?.let { wrapper ->
            CipherMemoryManager.stableRefPointer(wrapper, null, null)
        }
    }
)

internal val CipherAllocateCipherHandlers = createCipherAllocators(
    cipherAllocator(0),
    cipherAllocator(1),
    cipherAllocator(2),
    cipherAllocator(3)
)

@JsFun("(handler) => (p0) => handler(p0)")
private external fun cipherFreeCipher(handler: (cipher: WasmPointer) -> Unit): JsFunction

internal val CipherFreeCipherHandler = wasm.installFunction(
    signature = Void(Ptr),
    function = cipherFreeCipher { cipher ->
        CipherMemoryManager.getStableRef<Nothing>(cipher).let { reference ->
            (reference.data as CipherWrapper<*>).free().also {
                reference.dispose()
            }
        }
    }
)

/**
 * Returns the [CipherWrapper] backed by this opaque pointer.
 */
private inline fun WasmPointer.getCipherWrapper(): CipherWrapper<*> =
    CipherMemoryManager.stableRefData(this)

@JsFun("(handler) => (p0, p1) => handler(p0, p1)")
private external fun cipherCloneCipher(
    handler: (
        cipherTo: WasmPointer,
        cipherFrom: WasmPointer
    ) -> Unit
): JsFunction

internal val CipherCloneCipherHandler = wasm.installFunction(
    signature = Void(Ptr, Ptr),
    function = cipherCloneCipher { cipherTo, cipherFrom ->
        cipherTo.getCipherWrapper().clone(cipherFrom.getCipherWrapper())
    }
)

@JsFun("(handler) => (p0) => handler(p0)")
private external fun cipherGetParam(handler: (cipher: WasmPointer) -> Int): JsFunction

internal val CipherGetLegacyHandler = wasm.installFunction(
    signature = I32(Ptr),
    function = cipherGetParam { cipher ->
        cipher.getCipherWrapper().getLegacy()
    }
)

internal val CipherGetPageSizeHandler = wasm.installFunction(
    signature = I32(Ptr),
    function = cipherGetParam { cipher ->
        cipher.getCipherWrapper().getPageSize()
    }
)

internal val CipherGetReservedHandler = wasm.installFunction(
    signature = I32(Ptr),
    function = cipherGetParam { cipher ->
        cipher.getCipherWrapper().getReserved()
    }
)

@JsFun("(handler) => (p0) => handler(p0)")
private external fun cipherGetSalt(handler: (cipher: WasmPointer) -> WasmPointer): JsFunction

internal val CipherGetSaltHandler = wasm.installFunction(
    signature = Ptr(Ptr),
    function = cipherGetSalt { cipher ->
        cipher.getCipherWrapper().getSalt()?.pointer.notNull
    }
)

@JsFun("(handler) => (p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun cipherGenerateKey(
    handler: (
        cipher: WasmPointer,
        userPassword: WasmPointer,
        passwordLength: Int,
        rekey: Int,
        cipherSalt: WasmPointer
    ) -> Unit
): JsFunction

internal val CipherGenerateKeyHandler = wasm.installFunction(
    signature = Void(Ptr, Ptr, I32, I32, Ptr),
    function = cipherGenerateKey { cipher, userPassword, passwordLength, rekey, cipherSalt ->
        cipher.getCipherWrapper().run {
            generateKey(
                userPassword = userPassword.readBytes(passwordLength),
                rekey = rekey,
                cipherSalt = Buffer.from(cipherSalt, saltSize)
            )
        }
    }
)

@JsFun("(handler) => (p0, p1, p2, p3, p4) => handler(p0, p1, p2, p3, p4)")
private external fun cipherEncryptPage(
    handler: (
        cipher: WasmPointer,
        page: Int,
        data: WasmPointer,
        len: Int,
        reserved: Int
    ) -> Int
): JsFunction

internal val CipherEncryptPageHandler = wasm.installFunction(
    signature = I32(Ptr, I32, Ptr, I32, I32),
    function = cipherEncryptPage { cipher, page, data, len, reserved ->
        cipher.getCipherWrapper().encryptPage(
            page = page,
            data = Buffer.from(data, len.toLong())!!,
            reserved = reserved
        )
    }
)

@JsFun("(handler) => (p0, p1, p2, p3, p4, p5) => handler(p0, p1, p2, p3, p4, p5)")
private external fun cipherDecryptPage(
    handler: (
        cipher: WasmPointer,
        page: Int,
        data: WasmPointer,
        len: Int,
        reserved: Int,
        hmacCheck: Int
    ) -> Int
): JsFunction

internal val CipherDecryptPageHandler = wasm.installFunction(
    signature = I32(Ptr, I32, Ptr, I32, I32, I32),
    function = cipherDecryptPage { cipher, page, data, len, reserved, hmacCheck ->
        cipher.getCipherWrapper().decryptPage(
            page = page,
            data = Buffer.from(data, len.toLong())!!,
            reserved = reserved,
            hmacCheck = hmacCheck
        )
    }
)