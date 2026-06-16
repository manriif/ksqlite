package ksqlite.kapi.backup

import ksqlite.capi.sqlite3_backup_finish
import ksqlite.capi.sqlite3_backup_init
import ksqlite.capi.sqlite3_backup_pagecount
import ksqlite.capi.sqlite3_backup_remaining
import ksqlite.capi.sqlite3_backup_step
import ksqlite.capi.sqlite3_errcode
import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.types.sqlite3_backup
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.throwSQLiteException
import ksqlite.types.SqliteResultCode

internal class BackupImpl(private val backup: sqlite3_backup) :
    Backup,
    ClosableScope() {

    override val pageCount: Int
        get() = notClosed { sqlite3_backup_pagecount(backup) }

    override val remaining: Int
        get() = notClosed { sqlite3_backup_remaining(backup) }

    override fun step(count: Int) =
        notClosed { sqliteResultCheck(sqlite3_backup_step(backup, count)) }

    override fun onClose() = sqliteResultCheck(sqlite3_backup_finish(backup))
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
    val backup = sqlite3_backup_init(destination.db, destinationName, source.db, sourceName)

    if (backup == null) {
        val message = sqlite3_errmsg(destination.db) ?: "Failed to initializes a backup"
        val result = sqlite3_errcode(destination.db)

        check(result is SqliteResultCode.Failure) {
            "Unexpected result $result after a sqlite3_backup_init() failure"
        }

        throwSQLiteException(message, result)
    }

    return BackupImpl(backup)
}