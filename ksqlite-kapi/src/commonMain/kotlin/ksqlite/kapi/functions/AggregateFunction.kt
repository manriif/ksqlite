package ksqlite.kapi.functions

import ksqlite.kapi.value.ProtectedValue

/**
 * [Aggregate function](https://sqlite.org/appfunc.html#the_aggregate_function_callbacks).
 */
public interface AggregateFunction : Function {

    /**
     * This method is invoked to add a row to the current window. The function [arguments], if any,
     * corresponds to the row being added.
     */
    public fun AggregateFunctionStepScope.step(arguments: Array<ProtectedValue>)

    /**
     * This method is invoked to return the current value of the aggregate (determined by the
     * contents of the current window), and to free any resources allocated by earlier calls to
     * [step].
     */
    public fun AggregateFunctionFinalScope.final()
}