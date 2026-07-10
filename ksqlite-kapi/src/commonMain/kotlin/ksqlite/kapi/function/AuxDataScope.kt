package ksqlite.kapi.function

import ksqlite.capi.sqlite3_get_auxdata
import ksqlite.capi.sqlite3_set_auxdata
import ksqlite.kapi.helpers.autoCloser

/**
 * Scope for use with [ScalarFunction.func] and [WindowFunction.inverse]..
 */
public abstract class AuxDataScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) {

    /**
     * Returns the auxiliary data for the argument at [index] as [Data], or `null` if there is no
     * associated auxiliary or they have been discarded by SQLite.
     */
    public inline fun <reified Data : Any> getAuxDataOrNull(index: Int): Data? = scope.notClosed {
        sqlite3_get_auxdata<Data>(scope.context, index)
    }

    /**
     * Sets [data] as the auxiliary data for the argument at [index].
     *
     * If [data] implements [AutoCloseable] then [AutoCloseable.close] is invoked on it when SQLite
     * finalize it.
     */
    public fun setAuxData(index: Int, data: Any): Unit = scope.notClosed {
        sqlite3_set_auxdata(scope.context, index, data, autoCloser(data))
    }

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