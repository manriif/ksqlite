package ksqlite.kapi.functions

import ksqlite.capi.sqlite3_get_auxdata
import ksqlite.capi.sqlite3_set_auxdata
import ksqlite.kapi.helpers.autoCloser
import ksqlite.kapi.value.ValueReturnScopeImpl
import ksqlite.kapi.value.ValueReturnScope

/**
 * Scope for use with [ScalarFunction.func] and [WindowFunction.inverse].
 * Also see [FunctionScope] for error handling.
 *
 * Internal note: [ScalarFunctionFuncScope] is exposed as a class because of [getOrCreateAuxData]
 * which is required to be an inline function with a reified type.
 */
public class ScalarFunctionFuncScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) : FunctionScope by scope,
    ValueReturnScope by ValueReturnScopeImpl(scope) {

    /**
     * Returns the auxiliary data for the argument at [index] as [Data].
     *
     * The [Data] is created the first time the function is called using [compute] and is returned
     * on subsequent call.
     *
     * If [Data] implements [AutoCloseable] then [AutoCloseable.close] is invoked on the computed
     * instance when SQLite finalize it.
     */
    public inline fun <reified Data : Any> getOrCreateAuxData(
        index: Int,
        noinline compute: () -> Data
    ): Data = scope.notClosed {
        sqlite3_get_auxdata<Data>(scope.context, index) ?: run {
            compute().also { data ->
                sqlite3_set_auxdata(scope.context, index, data, autoCloser(data))
            }
        }
    }
}