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
@file:Suppress("ClassName", "PropertyName")

package ksqlite.structs

/**
 * Allocates or reinterprets a `ksqlite_cipher_params`.
 */
public abstract class ksqlite_cipher_params<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.KsqliteCipherParams, ksqlite_cipher_params.Member, Pointer>(
    type = KsqliteCipherParams,
    adapter = adapter,
    pointer = pointer
) {

    public var m_name: Pointer
        get() = readPointer(NAME)
        set(value) = writePointer(NAME, value)

    public var m_value: Int
        get() = readInt(VALUE)
        set(value) = writeInt(VALUE, value)

    public var m_default: Int
        get() = readInt(DEFAULT)
        set(value) = writeInt(DEFAULT, value)

    public var m_minValue: Int
        get() = readInt(MINVALUE)
        set(value) = writeInt(MINVALUE, value)

    public var m_maxValue: Int
        get() = readInt(MAXVALUE)
        set(value) = writeInt(MAXVALUE, value)

    /**
     * Members of the `ksqlite_cipher_params` struct.
     */
    public enum class Member : StructMember<StructType.KsqliteCipherParams> {
        NAME,
        VALUE,
        DEFAULT,
        MINVALUE,
        MAXVALUE,
    }
}