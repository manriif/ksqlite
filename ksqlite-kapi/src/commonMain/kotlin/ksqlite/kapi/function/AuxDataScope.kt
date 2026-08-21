/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * associated auxiliary data, or if it has been discarded by SQLite.
     */
    public inline fun <reified Data : Any> getAuxDataOrNull(index: Int): Data? = scope.notClosed {
        sqlite3_get_auxdata<Data>(scope.context, index)
    }

    /**
     * Sets [data] as the auxiliary data for the argument at [index].
     *
     * If [data] implements [AutoCloseable] then [AutoCloseable.close] is invoked on it when
     * SQLite finalizes it.
     */
    public fun setAuxData(index: Int, data: Any): Unit = scope.notClosed {
        sqlite3_set_auxdata(scope.context, index, data, autoCloser(data))
    }

    /**
     * Returns the auxiliary data for the argument at [index] as [Data].
     *
     * The [Data] is created the first time the function is called using [compute] and is returned
     * on subsequent calls.
     *
     * If [Data] implements [AutoCloseable] then [AutoCloseable.close] is invoked on the computed
     * instance when SQLite finalizes it.
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