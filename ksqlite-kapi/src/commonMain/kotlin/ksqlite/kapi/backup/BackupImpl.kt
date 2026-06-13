package ksqlite.kapi.backup

import ksqlite.capi.sqlite3_backup_finish
import ksqlite.capi.sqlite3_backup_pagecount
import ksqlite.capi.sqlite3_backup_remaining
import ksqlite.capi.sqlite3_backup_step
import ksqlite.capi.types.sqlite3_backup
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck

internal class BackupImpl(private val backup: sqlite3_backup) :
    Backup,
    ClosableScope() {

    override val pageCount: Int
        get() = notClosed { sqlite3_backup_pagecount(backup) }

    override val remaining: Int
        get() = notClosed { sqlite3_backup_remaining(backup) }

    override fun step(count: Int) =
        notClosed { sqliteResultCheck(sqlite3_backup_step(backup, count)) }

    override fun close() {
        if (!closed) {
            sqliteResultCheck(sqlite3_backup_finish(backup))
        }

        super.close()
    }
}