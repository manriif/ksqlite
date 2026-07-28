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
package ksqlite.types.cipher

import kotlin.enums.EnumEntries

/**
 * Parameter of a cipher config or global config.
 */
public interface SqliteMcConfigParam<Value : Any> {

    /**
     * Name of the parameter.
     */
    public val name: String

    /**
     * Returns the [Int] representing [value].
     */
    public fun toInt(value: Value?): Int

    /**
     * Returns the [Value] represented by [value].
     */
    public fun toValue(value: Int): Value?

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Value can be represented as integer.
     */
    public interface IntRepresentable {

        /**
         * Value as [Int].
         */
        public val value: Int
    }

    ///////////////////////////////////////////////////////////////////////////
    // Commons
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [SqliteMcConfigParam] accepting parameter of type [Byte].
     */
    public open class OfByte(override val name: String) : SqliteMcConfigParam<Byte> {

        override fun toInt(value: Byte?): Int = value?.toInt() ?: -1

        override fun toValue(value: Int): Byte? = value.takeIf { it >= 0 }?.toByte()
    }

    /**
     * [SqliteMcConfigParam] accepting parameter of type [Boolean].
     */
    public open class OfBoolean(override val name: String) : SqliteMcConfigParam<Boolean> {

        override fun toInt(value: Boolean?): Int = when (value) {
            true -> 1
            false -> 0
            null -> -1
        }

        override fun toValue(value: Int): Boolean? = when (value) {
            1 -> true
            0 -> false
            else -> null
        }
    }

    /**
     * [SqliteMcConfigParam] accepting parameter of type [Int].
     */
    public open class OfInt(override val name: String) : SqliteMcConfigParam<Int> {

        override fun toInt(value: Int?): Int = value ?: -1

        override fun toValue(value: Int): Int = value
    }

    /**
     * [SqliteMcConfigParam] accepting parameter of type [E].
     */
    public open class OfEnum<E>(
        override val name: String,
        private val entries: EnumEntries<E>
    ) : SqliteMcConfigParam<E> where E : Enum<E>, E : IntRepresentable {

        override fun toInt(value: E?): Int = value?.value ?: -1

        override fun toValue(value: Int): E? = entries.firstOrNull { it.value == value }
    }
}