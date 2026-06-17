package ksqlite.kapi.statement

import ksqlite.capi.sqlite3_bind_blob
import ksqlite.capi.sqlite3_bind_blob64
import ksqlite.capi.sqlite3_bind_double
import ksqlite.capi.sqlite3_bind_int
import ksqlite.capi.sqlite3_bind_int64
import ksqlite.capi.sqlite3_bind_null
import ksqlite.capi.sqlite3_bind_parameter_count
import ksqlite.capi.sqlite3_bind_parameter_index
import ksqlite.capi.sqlite3_bind_parameter_name
import ksqlite.capi.sqlite3_bind_pointer
import ksqlite.capi.sqlite3_bind_text
import ksqlite.capi.sqlite3_bind_text64
import ksqlite.capi.sqlite3_bind_value
import ksqlite.capi.sqlite3_bind_zeroblob
import ksqlite.capi.sqlite3_bind_zeroblob64
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.autoCloser
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.value.Value
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteTextEncoding

internal class StatementBindScopeImpl(private val stmt: sqlite3_stmt) :
    StatementBindScope,
    ClosableScope() {

    override val parameterCount: Int
        get() = notClosed { sqlite3_bind_parameter_count(stmt) }

    override fun parameterIndex(name: String): Int =
        notClosed { sqlite3_bind_parameter_index(stmt, name) }

    override fun parameterName(index: Int): String? =
        notClosed { sqlite3_bind_parameter_name(stmt, index) }

    /**
     * Invokes [block] throwing [ksqlite.kapi.SQLiteException] if it returns a failure code.
     */
    private inline fun bind(block: () -> SqliteResultCode) =
        notClosed { sqliteResultCheck(block()) }

    override fun bind(index: Int, value: Nothing?): Unit =
        bind { sqlite3_bind_null(stmt, index) }

    override fun bind(index: Int, value: Nothing?, size: Int) =
        bind { sqlite3_bind_zeroblob(stmt, index, size) }

    override fun bind(index: Int, value: Nothing?, size: ULong) =
        bind { sqlite3_bind_zeroblob64(stmt, index, size) }

    override fun bind(index: Int, value: ByteArray, size: Int) =
        bind { sqlite3_bind_blob(stmt, index, value, size, null) }

    override fun bind(
        index: Int,
        value: Buffer,
        size: Long,
        cleanup: ((Buffer) -> Unit)?
    ) = bind {
        sqlite3_bind_blob64(
            stmt = stmt,
            index = index,
            buffer = value.buffer,
            size = size,
            destroy = value.reference(cleanup)
        )
    }

    override fun bind(index: Int, value: Int) =
        bind { sqlite3_bind_int(stmt, index, value) }

    override fun bind(index: Int, value: Long) =
        bind { sqlite3_bind_int64(stmt, index, value) }

    override fun bind(index: Int, value: Double) =
        bind { sqlite3_bind_double(stmt, index, value) }

    override fun bind(index: Int, value: String) =
        bind { sqlite3_bind_text(stmt, index, value) }

    override fun bind(
        index: Int,
        value: Buffer,
        encoding: SqliteTextEncoding.BindText,
        size: Long,
        cleanup: ((Buffer) -> Unit)?
    ) = bind {
        sqlite3_bind_text64(
            stmt = stmt,
            index = index,
            buffer = value.buffer,
            size = size,
            encoding = encoding,
            destroy = value.reference(cleanup)
        )
    }

    override fun bind(index: Int, value: Value) =
        bind { sqlite3_bind_value(stmt, index, value.value) }

    override fun bind(index: Int, value: Any, type: String?) =
        bind { sqlite3_bind_pointer(stmt, index, value, type, autoCloser(value)) }
}