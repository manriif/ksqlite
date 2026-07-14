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
package ksqlite.kapi.database

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.ReadableBuffer

/**
 * Result for [DatabaseConnection.serialize].
 */
public sealed interface SerializeResult {

    /**
     * Size of the database.
     */
    public val databaseSize: Long

    /**
     * SQLite returned a `null` buffer but may have supplied the [databaseSize].
     */
    public class Failure(override val databaseSize: Long) : SerializeResult

    /**
     * SQLite owns the [buffer].
     */
    public class Immutable(public val buffer: ReadableBuffer) : SerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }

    /**
     * The application is responsible for freeing the [buffer].
     */
    public class Mutable(public val buffer: Buffer) : SerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }
}