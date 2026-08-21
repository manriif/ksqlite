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
package ksqlite.kapi.connection

/**
 * Callback to use with [DatabaseConnection.setAutovacuumPages].
 */
public fun interface AutovacuumPages : AutoCloseable {

    /**
     * Called before each autovacuum of the database identified by [schemaName]. [dbPage] is the
     * current size of the database in pages, [freePage] the number of pages currently free and
     * available for reuse, and [bytePerPage] the number of bytes per page. Returns the number of
     * pages to autovacuum during this pass, `0u` to skip it entirely.
     */
    public fun apply(
        schemaName: String,
        dbPage: UInt,
        freePage: UInt,
        bytePerPage: UInt
    ): UInt

    /**
     * Called when the callback is no longer needed by SQLite.
     */
    override fun close(): Unit = Unit
}