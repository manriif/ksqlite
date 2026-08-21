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

import ksqlite.kapi.result.Result
import ksqlite.kapi.value.ProtectedValue

/**
 * [Window Function](https://sqlite.org/windowfunctions.html#user_defined_aggregate_window_functions)
 */
public interface WindowFunction : AggregateFunction {

    /**
     * Removes from the current window the oldest row previously aggregated by [step].
     * [arguments], if any, are those that were passed to [step] for that row.
     */
    public fun WindowFunctionInverseScope.inverse(arguments: Array<ProtectedValue>)

    /**
     * Returns the current value of the aggregate without freeing the context, unlike [final].
     * The returned [Result] must come from calling one of the receiver's result methods.
     */
    public fun AggregateFunctionFinalScope.value(): Result
}