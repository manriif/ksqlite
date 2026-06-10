package ksqlite.kapi.impl.functions

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.sqlite3_context_db_handle
import ksqlite.capi.sqlite3_result_blob
import ksqlite.capi.sqlite3_result_blob64
import ksqlite.capi.sqlite3_result_double
import ksqlite.capi.sqlite3_result_error
import ksqlite.capi.sqlite3_result_error_code
import ksqlite.capi.sqlite3_result_error_nomem
import ksqlite.capi.sqlite3_result_error_toobig
import ksqlite.capi.sqlite3_result_int
import ksqlite.capi.sqlite3_result_int64
import ksqlite.capi.sqlite3_result_null
import ksqlite.capi.sqlite3_result_pointer
import ksqlite.capi.sqlite3_result_text
import ksqlite.capi.sqlite3_result_text64
import ksqlite.capi.sqlite3_result_value
import ksqlite.capi.sqlite3_result_zeroblob
import ksqlite.capi.sqlite3_result_zeroblob64
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.sqlite3_context
import ksqlite.kapi.SQLiteConnection
import ksqlite.kapi.SQLiteValue
import ksqlite.kapi.functions.FunctionResultScope
import ksqlite.kapi.functions.FunctionScope
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.impl.retrieveConnection
import ksqlite.kapi.sqliteResultCheck

@PublishedApi
internal open class FunctionScopeImpl(
    @PublishedApi
    internal val context: sqlite3_context
) : FunctionScope,
    ClosableScope() {

    final override val connection: SQLiteConnection
        get() = notClosed { retrieveConnection(sqlite3_context_db_handle(context)) }
}

@PublishedApi
internal open class FunctionResultScopeImpl(context: sqlite3_context) :
    FunctionScopeImpl(context),
    FunctionResultScope {

    override fun setResult(value: Nothing?) =
        notClosed { sqlite3_result_null(context) }

    override fun setResult(value: Nothing?, size: Int) =
        notClosed { sqlite3_result_zeroblob(context, size) }

    override fun setResult(value: Nothing?, size: ULong) = notClosed {
        sqliteResultCheck(
            result = sqlite3_result_zeroblob64(context, size),
            getDb = { sqlite3_context_db_handle(context) }
        )
    }

    override fun setResult(value: ByteArray, size: Int) =
        notClosed { sqlite3_result_blob(context, value, size, null) }

    override fun setResult(value: Int) =
        notClosed { sqlite3_result_int(context, value) }

    override fun setResult(value: Long) =
        notClosed { sqlite3_result_int64(context, value) }

    override fun setResult(value: Double) =
        notClosed { sqlite3_result_double(context, value) }

    override fun setResult(value: String) =
        notClosed { sqlite3_result_text(context, value) }

    override fun setResult(
        value: Buffer,
        encoding: Sqlite3TextEncoding.Set1?,
        size: Long,
        cleanup: ((Buffer) -> Unit)?
    ) = notClosed {
        val destructor = cleanup?.let(::Sqlite3DestroyCallback)

        if (encoding != null) {
            sqlite3_result_text64(context, value, size, encoding, destructor)
        } else {
            sqlite3_result_blob64(context, value, size, destructor)
        }
    }

    override fun setResult(value: SQLiteValue) =
        notClosed { sqlite3_result_value(context, value.value) }

    override fun setResult(value: Any, type: String?) =
        notClosed { sqlite3_result_pointer(context, value, type, autoClosableDestructor(value)) }

    override fun setResultError(
        message: String,
        result: Sqlite3Result.Failure?
    ): Unit = notClosed {
        sqlite3_result_error(context, message)
        result?.let { sqlite3_result_error_code(context, it) }
    }

    override fun setResultErrorNoMem() = notClosed { sqlite3_result_error_nomem(context) }

    override fun setResultErrorTooBig() = notClosed { sqlite3_result_error_toobig(context) }
}