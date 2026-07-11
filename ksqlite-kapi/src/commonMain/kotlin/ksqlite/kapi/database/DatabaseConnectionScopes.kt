package ksqlite.kapi.database

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_preupdate_blobwrite
import ksqlite.capi.sqlite3_preupdate_count
import ksqlite.capi.sqlite3_preupdate_depth
import ksqlite.capi.sqlite3_preupdate_new
import ksqlite.capi.sqlite3_preupdate_old
import ksqlite.capi.sqlite3_value
import ksqlite.kapi.helpers.UnsafeClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.kapi.value.ProtectedValue
import ksqlite.kapi.value.toProtectedValue

internal class PreupdateHookScopeImpl(private val db: sqlite3) :
    PreupdateHookScope,
    UnsafeClosableScope() {

    override val count: Int
        get() = notClosed { sqlite3_preupdate_count(db) }

    override val depth: Int
        get() = notClosed { sqlite3_preupdate_depth(db) }

    override val blobColumnIndex: Int
        get() = notClosed { sqlite3_preupdate_blobwrite(db) }

    override fun oldValue(index: Int): ProtectedValue = notClosed {
        usingParam(sqlite3_value.OutputParam()) { outValue ->
            sqliteResultCheck(sqlite3_preupdate_old(db, index, outValue))
        }.toProtectedValue(this)
    }

    override fun newValue(index: Int): ProtectedValue = notClosed {
        usingParam(sqlite3_value.OutputParam()) { outValue ->
            sqliteResultCheck(sqlite3_preupdate_new(db, index, outValue))
        }.toProtectedValue(this)
    }
}