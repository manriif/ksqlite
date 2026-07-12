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