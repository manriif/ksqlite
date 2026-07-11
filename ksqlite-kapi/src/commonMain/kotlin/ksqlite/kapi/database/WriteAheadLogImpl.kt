package ksqlite.kapi.database

import ksqlite.capi.sqlite3_wal_autocheckpoint
import ksqlite.capi.sqlite3_wal_checkpoint_v2
import ksqlite.capi.sqlite3_wal_hook
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.sqlite3
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParams
import ksqlite.types.SqliteCheckpointMode

internal class WriteAheadLogImpl(
    private val db: sqlite3,
    private val scope: ClosableScope
) : WriteAheadLog {

    override fun autoCheckpoint(frameCount: Int) =
        scope.notClosed { sqliteResultCheck(sqlite3_wal_autocheckpoint(db, frameCount)) }

    override fun checkpoint(
        mode: SqliteCheckpointMode,
        database: String?
    ): WriteAheadLogCheckpointResult = scope.notClosed {
        usingParams(
            param1 = Int32OutputParam(0),
            param2 = Int32OutputParam(0),
            transform = ::WriteAheadLogCheckpointResultImpl
        ) { outNLog, outNCkpt ->
            db.resultCheck(sqlite3_wal_checkpoint_v2(db, database, mode, outNLog, outNCkpt))
        }
    }

    override fun setHook(hook: WriteAheadLogHook?) = scope.notClosed {
        if (hook != null) {
            sqlite3_wal_hook(db, hook, WriteAheadLogHookCallback)
        } else {
            sqlite3_wal_hook(db, null, null)
        }
    }
}