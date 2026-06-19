package ksqlite.kapi.function

import ksqlite.capi.sqlite3_aggregate_context
import ksqlite.kapi.value.ValueReturnScopeImpl
import ksqlite.kapi.value.ValueReturnScope

/**
 * Scope for use with [AggregateFunction.final] and [WindowFunction.value].
 * Also see [FunctionScope] for error handling.
 */
public class AggregateFunctionFinalScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) : FunctionScope by scope,
    ValueReturnScope by ValueReturnScopeImpl(scope) {

    /**
     * Returns the aggregate context, if any, as [Ctx].
     */
    public inline fun <reified Ctx : Any> getContextOrNull(): Ctx? {
        return scope.notClosed { sqlite3_aggregate_context(scope.context, null) }
    }
}