package ksqlite.kapi.database

import ksqlite.capi.sqlite3_errcode
import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.sqlite3_error_offset
import ksqlite.capi.sqlite3_extended_errcode
import ksqlite.capi.sqlite3_set_errmsg
import ksqlite.capi.sqlite3_system_errno
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.types.SqliteResultCode

internal class LastErrorImpl(
    private val db: sqlite3,
    private val scope: ClosableScope
) : LastError {

    override val message: String?
        get() = scope.notClosed { sqlite3_errmsg(db) }

    override val code: SqliteResultCode
        get() = scope.notClosed { sqlite3_errcode(db) }

    override val extendedCode: SqliteResultCode
        get() = scope.notClosed { sqlite3_extended_errcode(db) }

    override val offset: Int
        get() = scope.notClosed { sqlite3_error_offset(db) }

    override val systemError: Int
        get() = scope.notClosed { sqlite3_system_errno(db) }

    override fun update(
        errorCode: SqliteResultCode,
        errorMessage: String?
    ) = scope.notClosed { sqliteResultCheck(sqlite3_set_errmsg(db, errorCode, errorMessage)) }
}