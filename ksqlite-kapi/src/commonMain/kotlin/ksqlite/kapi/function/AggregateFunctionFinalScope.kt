package ksqlite.kapi.function

import ksqlite.capi.sqlite3_aggregate_context
import ksqlite.kapi.value.ValueReturnScopeImpl
import ksqlite.kapi.value.ValueReturnScope

/**
 * Scope for use with [AggregateFunction.final], [WindowFunction.final] and [WindowFunction.value].
 */
public class AggregateFunctionFinalScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) : FunctionScope by scope,
    ValueReturnScope by ValueReturnScopeImpl(scope) {

    /**
     * Returns the aggregate context, if any, as [C].
     */
    public inline fun <reified C : Any> getContextOrNull(): C? {
        return scope.notClosed { sqlite3_aggregate_context(scope.context, null) }
    }
}