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
package ksqlite.kapi.cipher

import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcConfigCipherParam
import ksqlite.types.cipher.SqliteMcConfigParamPrefix

/**
 * Reads and writes the parameter values of a [Cipher]. See [CipherConfiguration] for how [prefix]
 * affects whether a read or write is transient or permanent.
 */
public interface CipherParameters<Cipher : SqliteMcCipher> {

    /**
     * Returns the value of [param].
     *
     * @throws ksqlite.kapi.SQLiteException if reading the value fails.
     */
    public fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> get(
        param: Param,
        prefix: SqliteMcConfigParamPrefix
    ): Value

    /**
     * Returns the transient value of [param].
     *
     * @throws ksqlite.kapi.SQLiteException if reading the value fails.
     */
    public operator fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> get(
        param: Param
    ): Value = get(param, None)

    /**
     * Sets [param] to [value] and returns the resulting parameter value.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the value fails.
     */
    public fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> set(
        param: Param,
        value: Value,
        prefix: SqliteMcConfigParamPrefix.ReadWrite
    )

    /**
     * Sets [param] to [value] permanently.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the value fails.
     */
    public operator fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> set(
        param: Param,
        value: Value
    ): Unit = set(param, value, Default)
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the transient value of the dynamic cipher parameter named [param].
 *
 * @throws ksqlite.kapi.SQLiteException if reading the value fails.
 */
public fun CipherParameters<SqliteMcCipher.Dynamic>.get(
    param: String,
    prefix: SqliteMcConfigParamPrefix.ReadWrite = SqliteMcConfigParamPrefix.None
): Int = get(
    param = SqliteMcCipher.Dynamic.Parameter(param),
    prefix = prefix
)

/**
 * Sets the dynamic cipher parameter named [param] to [value], permanently.
 *
 * @throws ksqlite.kapi.SQLiteException if writing the value fails.
 */
public fun CipherParameters<SqliteMcCipher.Dynamic>.set(
    param: String,
    value: Int,
    prefix: SqliteMcConfigParamPrefix.ReadWrite = SqliteMcConfigParamPrefix.Default
): Unit = set(
    param = SqliteMcCipher.Dynamic.Parameter(param),
    value = value,
    prefix = prefix
)