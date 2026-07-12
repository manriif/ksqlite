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
import ksqlite.kapi.value.ValueReturnScopeImpl
import ksqlite.kapi.value.ValueReturnScope

/**
 * Scope for use with [AggregateFunction.final], [WindowFunction.final] and [WindowFunction.value].
 */
public class AggregateFunctionFinalScope internal constructor(
    @PublishedApi
    internal val scope: FunctionScopeImpl
) : FunctionScope by scope,
    ValueReturnScope by ValueReturnScopeImpl(scope) {

    /**
     * Returns the aggregate context, if any, as [C].
     */
    public inline fun <reified C : Any> getContextOrNull(): C? {
        return scope.notClosed { sqlite3_aggregate_context(scope.context, null) }
    }
}