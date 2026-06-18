package ksqlite.kapi.database

import ksqlite.capi.sqlite3_preupdate_blobwrite
import ksqlite.capi.sqlite3_preupdate_count
import ksqlite.capi.sqlite3_preupdate_depth
import ksqlite.capi.sqlite3_preupdate_new
import ksqlite.capi.sqlite3_preupdate_old
import ksqlite.capi.types.SqliteValueOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.kapi.value.ProtectedValue
import ksqlite.kapi.value.toProtectedValue

/**
 * Special exception emitted to abort SQL statement execution.
 */
internal class ExecAbortException : Exception()

internal object ExecScopeImpl : ExecScope {

    override fun abort(): Nothing {
        throw ExecAbortException()
    }
}

internal class PreupdateHookScopeImpl(private val db: sqlite3) :
    PreupdateHookScope,
    ClosableScope() {

    override val count: Int
        get() = notClosed { sqlite3_preupdate_count(db) }

    override val depth: Int
        get() = notClosed { sqlite3_preupdate_depth(db) }

    override val blobColumnIndex: Int
        get() = notClosed { sqlite3_preupdate_blobwrite(db) }

    override fun oldValue(index: Int): ProtectedValue = notClosed {
        usingParam(SqliteValueOutputParam()) { outValue ->
            sqliteResultCheck(sqlite3_preupdate_old(db, index, outValue))
        }.toProtectedValue(this)
    }

    override fun newValue(index: Int): ProtectedValue = notClosed {
        usingParam(SqliteValueOutputParam()) { outValue ->
            sqliteResultCheck(sqlite3_preupdate_new(db, index, outValue))
        }.toProtectedValue(this)
    }
}