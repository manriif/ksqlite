package ksqlite.kapi.functions

import ksqlite.capi.sqlite3_aggregate_context
import ksqlite.kapi.impl.functions.FunctionResultScopeImpl

/**
 * Scope for use with [AggregateFunction.final] and [WindowFunction.value].
 *
 * Internal note: [AggregateFunctionFinalScope] is exposed as a class because of [getContextOrNull]
 * which is required to be an inline function with a reified type.
 */
public class AggregateFunctionFinalScope internal constructor(
    @PublishedApi
    internal val scope: FunctionResultScopeImpl
) : FunctionResultScope by scope {

    /**
     * Returns the aggregate context as [Data].
     *
     * The context is created the first time the function is called and is returned on subsequent
     * call.
     */
    public inline fun <reified Data : Any> getContextOrNull(): Data? {
        return scope.notClosed { sqlite3_aggregate_context(scope.context, null) }
    }
}