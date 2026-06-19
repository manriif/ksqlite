package ksqlite.kapi.function

import ksqlite.capi.sqlite3_aggregate_context
import ksqlite.kapi.helpers.sqliteOutOfMemoryCheck

/**
 * Scope for use with [AggregateFunction.step] and [WindowFunction.inverse].
 */
public class AggregateFunctionStepScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) : FunctionScope by scope {

    /**
     * Returns the aggregate context as [Ctx].
     *
     * The [Ctx] is created the first time the function is called using [compute] and is returned
     * on subsequent call.
     */
    public inline fun <reified Ctx : Any> getOrCreateContext(noinline compute: () -> Ctx): Ctx {
        return scope.notClosed {
            sqliteOutOfMemoryCheck(sqlite3_aggregate_context(scope.context, compute)) {
                "There is not enough memory available to allocate an aggregate context"
            }
        }
    }
}