package ksqlite.kapi.function

import ksqlite.capi.sqlite3_aggregate_context

/**
 * Scope for use with [WindowFunction.inverse].
 *
 * TODO: expose getAuxDataOrNull ?
 */
public class WindowFunctionInverseScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) : FunctionScope by scope {

    /**
     * Returns the aggregate context, if any, as [C].
     */
    public inline fun <reified C : Any> getAggregateContextOrNull(): C? =
        scope.notClosed { sqlite3_aggregate_context(scope.context, null) }
}