package ksqlite.kapi.functions

import ksqlite.kapi.value.ProtectedValue

/**
 * [Window Function](https://sqlite.org/windowfunctions.html#user_defined_aggregate_window_functions)
 */
public interface WindowFunction : AggregateFunction {

    /**
     * This method is invoked to remove the oldest presently aggregated result of [step] from the
     * current window. The function arguments, if any, are those passed to [step] for the row being
     * removed.
     */
    public fun AggregateFunctionStepScope.inverse(arguments: Array<ProtectedValue>)

    /**
     * This method is invoked to return the current value of the aggregate. Unlike [final], the
     * implementation should not delete any context.
     */
    public fun AggregateFunctionFinalScope.value()
}