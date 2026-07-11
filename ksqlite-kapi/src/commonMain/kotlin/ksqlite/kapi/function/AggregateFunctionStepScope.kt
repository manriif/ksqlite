package ksqlite.kapi.function

import ksqlite.capi.sqlite3_aggregate_context
import ksqlite.kapi.helpers.sqliteOutOfMemoryCheck

/**
 * Scope for use with [AggregateFunction.step] and [WindowFunction.step].
 */
public class AggregateFunctionStepScope internal constructor(scope: FunctionScopeImpl) :
    FunctionScope by scope,
    AuxDataScope(scope) {

    /**
     * Returns the aggregate context as [C].
     *
     * The [C] is created the first time the function is called using [compute] and is returned
     * on subsequent call.
     */
    public inline fun <reified C : Any> getOrCreateAggregateContext(noinline compute: () -> C): C {
        return scope.notClosed {
            sqliteOutOfMemoryCheck(sqlite3_aggregate_context(scope.context, compute)) {
                "There is not enough memory available to allocate an aggregate context"
            }
        }
    }
}