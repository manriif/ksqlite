package ksqlite.kapi.functions

/**
 * Scope for use with [ScalarFunction.func], [AggregateFunction.final] and [WindowFunction.value].
 */
public interface FunctionResultScope : FunctionScope {

    public fun result(value: Int)

    public fun result(value: Long)

    public fun result(value: Double)

    public fun result(value: String)
}