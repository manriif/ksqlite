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
@file:Suppress("PropertyName")

package ksqlite.types.cipher

/**
 * Describes a
 * [`CipherParams`](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/#cipher-configuration-parameters)
 * struct.
 */
public interface SqliteMcCipherParams {

    /**
     * Name of the parameter.
     *
     * The first character must be alphabetic = alpha, all other characters may be alphanumeric or
     * underscore. The name may consist of a maximum of 63 characters.
     */
    public var m_name: String

    /**
     * Current/transient parameter value.
     */
    public var m_value: Int

    /**
     * Default parameter value.
     */
    public var m_default: Int

    /**
     * Minimum valid parameter value.
     */
    public var m_minValue: Int

    /**
     * Maximum valid parameter value.
     */
    public var m_maxValue: Int
}