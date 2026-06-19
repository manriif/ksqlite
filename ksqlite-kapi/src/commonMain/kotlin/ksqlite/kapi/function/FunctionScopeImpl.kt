package ksqlite.kapi.function

import ksqlite.capi.sqlite3_context_db_handle
import ksqlite.capi.sqlite3_result_error
import ksqlite.capi.sqlite3_result_error_code
import ksqlite.capi.sqlite3_result_error_nomem
import ksqlite.capi.sqlite3_result_error_toobig
import ksqlite.types.SqliteResultCode
import ksqlite.capi.types.sqlite3_context
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.helpers.ContextClosableScope
import ksqlite.kapi.sqliteRequireConnection
import ksqlite.kapi.throwSQLiteException

/**
 * Special exception types used to set the result of a function call.
 */
private sealed class ResultException : SQLiteException(SqliteResultCode.ERROR, "") {
    class NoMem : ResultException()
    class TooBig : ResultException()
}

@PublishedApi
internal class FunctionScopeImpl(context: sqlite3_context) :
    FunctionScope,
    ContextClosableScope(context) {

    override val connection: DatabaseConnection
        get() = notClosed { sqliteRequireConnection(sqlite3_context_db_handle(context)) }

    override fun setResultError(
        message: String,
        result: SqliteResultCode.Failure
    ): Nothing = notClosed { throwSQLiteException(message, result) }

    override fun setResultErrorNoMem(): Nothing = notClosed { throw ResultException.NoMem() }

    override fun setResultErrorTooBig(): Nothing = notClosed { throw ResultException.TooBig() }

    fun handleError(exception: SQLiteException) {
        when (exception) {
            is ResultException -> when (exception) {
                is ResultException.NoMem -> sqlite3_result_error_nomem(context)
                is ResultException.TooBig -> sqlite3_result_error_toobig(context)
            }

            else -> {
                sqlite3_result_error(context, exception.message)
                sqlite3_result_error_code(context, exception.result)
            }
        }
    }
}