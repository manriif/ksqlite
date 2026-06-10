package ksqlite.kapi.functions

import ksqlite.capi.sqlite3_aggregate_context
import ksqlite.kapi.checkOutOfMemory
import ksqlite.kapi.impl.functions.FunctionScopeImpl

/**
 * Scope for use with [AggregateFunction.step] and [WindowFunction.inverse].
 *
 * Internal note: [AggregateFunctionStepScope] is exposed as a class because of [getOrCreateContext]
 * which is required to be an inline function with a reified type.
 */
public class AggregateFunctionStepScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) : FunctionScope by scope {

    /**
     * Returns the aggregate context as [Data].
     *
     * The context is created the first time the function is called and is returned on subsequent
     * call.
     */
    public inline fun <reified Data : Any> getOrCreateContext(noinline compute: () -> Data): Data {
        return scope.notClosed {
            checkOutOfMemory(sqlite3_aggregate_context(scope.context, compute)) {
                "There is not enough memory available to allocate an aggregate context"
            }
        }
    }
}