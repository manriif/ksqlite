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
@file:Suppress("ClassName")

package ksqlite.types

/**
 * These constants define integer codes that represent the various text encodings supported by
 * SQLite.
 *
 * [Text Encodings](https://sqlite.org/c3ref/c_any.html)
 *
 * TODO: uncomment constants subclasses after UTF16 support is added
 */
public sealed interface SqliteTextEncoding {

    /**
     * Encoding value.
     */
    public val value: Int

    ///////////////////////////////////////////////////////////////////////////
    // Groups
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Encoding supported by the `sqlite3_bind_text64()` routine.
     */
    public sealed interface BindText : SqliteTextEncoding

    /**
     * Encoding that is passed to the `sqlite3_collation_needed()` callback.
     */
    public sealed interface CollationNeeded : SqliteTextEncoding

    /**
     * Encoding supported by the `sqlite3_create_collation()` routine.
     */
    public sealed interface CreateCollation : SqliteTextEncoding

    /**
     * Encoding supported by the `sqlite3_result_text64()` routine.
     */
    public sealed interface ResultText : SqliteTextEncoding

    /**
     * Encoding that is returned by the `sqlite3_value_encoding()` routine.
     */
    public sealed interface ValueEncoding : SqliteTextEncoding

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public sealed class Constant(override val value: Int) : SqliteTextEncoding

    /**
     * IMP: R-37514-35566.
     */
    public data object UTF8 :
        Constant(1),
        SqliteFunctionTextEncoding,
        BindText,
        CollationNeeded,
        CreateCollation,
        ResultText,
        ValueEncoding

    /**
     * IMP: R-03371-37637.
     */
    public data object UFT16LE :
        Constant(2)/*,
        SqliteFunctionTextEncoding,
        BindText,
        CollationNeeded,
        CreateCollation,
        ResultText,
        ValueEncoding*/

    /**
     * IMP: R-51971-34154
     */
    public data object UTF16BE :
        Constant(3)/*,
        SqliteFunctionTextEncoding,
        BindText,
        CollationNeeded,
        CreateCollation,
        ResultText,
        ValueEncoding*/

    /**
     * Use native byte order.
     */
    public data object UTF16 :
        Constant(4)/*,
        SqliteFunctionTextEncoding,
        BindText,
        CreateCollation,
        ResultText*/

    /**
     * sqlite3_create_collation() only.
     */
    public data object UTF16_ALIGNED :
        Constant(8)/*,
        CreateCollation*/

    /**
     * Zero-terminated UTF8.
     */
    public data object UTF8_ZT :
        Constant(16)/*,
        BindText,
        ResultText*/
}