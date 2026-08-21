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

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_backup
import ksqlite.capi.sqlite3_backup_finish
import ksqlite.capi.sqlite3_backup_init
import ksqlite.capi.sqlite3_backup_pagecount
import ksqlite.capi.sqlite3_backup_remaining
import ksqlite.capi.sqlite3_backup_step
import ksqlite.capi.sqlite3_errcode
import ksqlite.capi.sqlite3_errmsg
import ksqlite.internal.runtime.closeable.UnsafeCloseableScope
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.connection.impl
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.throwSQLiteException
import ksqlite.types.SqliteResultCode

internal class BackupImpl(
    private val destinationDb: sqlite3,
    private val backup: sqlite3_backup
) : Backup,
    UnsafeCloseableScope() {

    override val pageCount: Int
        get() = notClosed { sqlite3_backup_pagecount(backup) }

    override val remaining: Int
        get() = notClosed { sqlite3_backup_remaining(backup) }

    override fun step(count: Int) =
        notClosed { destinationDb.resultCheck(sqlite3_backup_step(backup, count)) }

    override fun onClose() = destinationDb.resultCheck(sqlite3_backup_finish(backup))
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Creates a new [Backup].
 */
internal fun createBackup(
    destination: DatabaseConnection,
    destinationName: String,
    source: DatabaseConnection,
    sourceName: String
): Backup {
    val destinationDb = destination.impl.db
    val sourceDb = source.impl.db

    val backup = sqlite3_backup_init(destinationDb, destinationName, sourceDb, sourceName)

    if (backup == null) {
        val message = sqlite3_errmsg(destinationDb) ?: "Failed to initializes a backup"
        val result = sqlite3_errcode(destinationDb)

        check(result is SqliteResultCode.Failure) {
            "Unexpected result $result after a sqlite3_backup_init() failure"
        }

        throwSQLiteException(message, result)
    }

    return BackupImpl(destinationDb, backup)
}