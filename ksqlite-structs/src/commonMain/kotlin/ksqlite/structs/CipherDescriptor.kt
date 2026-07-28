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
@file:Suppress("ClassName", "SpellCheckingInspection", "PropertyName")

package ksqlite.structs

/**
 * Allocates or reinterpets a `ksqlite_cipher_descriptor`.
 */
public abstract class ksqlite_cipher_descriptor<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.KsqliteCipherDescriptor, ksqlite_cipher_descriptor.Member, Pointer>(
    type = KsqliteCipherDescriptor,
    adapter = adapter,
    pointer = pointer
) {

    public var m_name: Pointer
        get() = readPointer(NAME)
        set(value) = writePointer(NAME, value)

    public var m_allocateCipher: Pointer
        get() = readPointer(ALLOCATECIPHER)
        set(value) = writePointer(ALLOCATECIPHER, value)

    public var m_freeCipher: Pointer
        get() = readPointer(FREECIPHER)
        set(value) = writePointer(FREECIPHER, value)

    public var m_cloneCipher: Pointer
        get() = readPointer(CLONECIPHER)
        set(value) = writePointer(CLONECIPHER, value)

    public var m_getLegacy: Pointer
        get() = readPointer(GETLEGACY)
        set(value) = writePointer(GETLEGACY, value)

    public var m_getPageSize: Pointer
        get() = readPointer(GETPAGESIZE)
        set(value) = writePointer(GETPAGESIZE, value)

    public var m_getReserved: Pointer
        get() = readPointer(GETRESERVED)
        set(value) = writePointer(GETRESERVED, value)

    public var m_getSalt: Pointer
        get() = readPointer(GETSALT)
        set(value) = writePointer(GETSALT, value)

    public var m_generateKey: Pointer
        get() = readPointer(GENERATEKEY)
        set(value) = writePointer(GENERATEKEY, value)

    public var m_encryptPage: Pointer
        get() = readPointer(ENCRYPTPAGE)
        set(value) = writePointer(ENCRYPTPAGE, value)

    public var m_decryptPage: Pointer
        get() = readPointer(DECRYPTPAGE)
        set(value) = writePointer(DECRYPTPAGE, value)

    /**
     * Members of the `ksqlite_cipher_descriptor` struct.
     */
    public enum class Member : StructMember<StructType.KsqliteCipherDescriptor> {
        NAME,
        ALLOCATECIPHER,
        FREECIPHER,
        CLONECIPHER,
        GETLEGACY,
        GETPAGESIZE,
        GETRESERVED,
        GETSALT,
        GENERATEKEY,
        ENCRYPTPAGE,
        DECRYPTPAGE,
    }
}