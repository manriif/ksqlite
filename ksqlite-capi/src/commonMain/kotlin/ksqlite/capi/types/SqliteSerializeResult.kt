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
package ksqlite.capi.types

import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.ReadableBuffer

/**
 * Result for [ksqlite.capi.sqlite3_serialize].
 */
public sealed interface SqliteSerializeResult {

    /**
     * Size of the database.
     */
    public val databaseSize: Long

    /**
     * SQLite returned a `null` buffer but may have supplied the [databaseSize].
     */
    public class Failure(override val databaseSize: Long) : SqliteSerializeResult

    /**
     * SQLite owns the [buffer].
     */
    public class Immutable(public val buffer: ReadableBuffer) : SqliteSerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }

    /**
     * The application is responsible for freeing the [buffer].
     */
    public class Mutable(public val buffer: Buffer) : SqliteSerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }
}