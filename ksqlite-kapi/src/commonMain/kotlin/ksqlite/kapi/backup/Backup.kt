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
package ksqlite.kapi.backup

import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.database.SQLITE_MAIN_DB_NAME

/**
 * Exposes the [Online Backup API](https://sqlite.org/backup.html).
 */
public interface Backup : AutoCloseable {

    /**
     * Returns the total number of pages in the source database as of the most recent call to [step].
     */
    public val pageCount: Int

    /**
     * Returns the number of pages still to be backed up as of the most recent call to [step].
     */
    public val remaining: Int

    /**
     * Copies up to [count] pages between the source and destination databases.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun step(count: Int)

    /**
     * Releases all resources associated with `this` backup.
     *
     * @throws ksqlite.kapi.SQLiteException if finalizing the backup fails.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides methods to initializes a [Backup].
     */
    public companion object {

        /**
         * Initializes a backup, copying content from database [sourceName] using the connection
         * [source] into the database [destinationName] using connection [destination] and returns
         * a [Backup].
         *
         * The returned [Backup] must be closed when done with.
         */
        public fun init(
            destination: DatabaseConnection,
            destinationName: String,
            source: DatabaseConnection,
            sourceName: String
        ): Backup = createBackup(
            destination = destination,
            destinationName = destinationName,
            source = source,
            sourceName = sourceName
        )

        /**
         * Initializes a backup, copying content from database `main` using the connection [source]
         * into the database `main` using connection [destination] and returns a [Backup].
         *
         * The returned [Backup] must be closed when done with.
         */
        public fun init(
            destination: DatabaseConnection,
            source: DatabaseConnection
        ): Backup = createBackup(
            destination = destination,
            destinationName = SQLITE_MAIN_DB_NAME,
            source = source,
            sourceName = SQLITE_MAIN_DB_NAME
        )
    }
}