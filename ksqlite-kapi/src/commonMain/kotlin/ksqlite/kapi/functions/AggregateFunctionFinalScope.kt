package ksqlite.kapi.functions

import ksqlite.capi.sqlite3_aggregate_context
import ksqlite.kapi.value.ValueReturnScopeImpl
import ksqlite.kapi.value.ValueReturnScope

/**
 * Scope for use with [AggregateFunction.final] and [WindowFunction.value].
 * Also see [FunctionScope] for error handling.
 *
 * Internal note: [AggregateFunctionFinalScope] is exposed as a class because of [getContextOrNull]
 * which is required to be an inline function with a reified type.
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