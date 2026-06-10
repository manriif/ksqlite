package ksqlite.kapi.impl.functions

import ksqlite.capi.callbacks.Sqlite3FunctionStepCallback
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.kapi.functions.AggregateFunction

/**
 * Invokes [AggregateFunction.step].
 */
internal object FunctionStepApplier : Sqlite3FunctionStepCallback<AggregateFunction<*>> {

    override fun apply(
        appData: AggregateFunction<*>,
        context: sqlite3_context,
        arguments: Array<sqlite3_value>
    ) {
        val scope = FunctionScopeImpl()
    }
}