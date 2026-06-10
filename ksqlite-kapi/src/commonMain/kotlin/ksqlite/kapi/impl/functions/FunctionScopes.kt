package ksqlite.kapi.impl.functions

import ksqlite.capi.sqlite3_context_db_handle
import ksqlite.capi.types.sqlite3_context
import ksqlite.kapi.SQLiteConnection
import ksqlite.kapi.functions.FunctionResultScope
import ksqlite.kapi.functions.FunctionScope
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.impl.retrieveConnection

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

}