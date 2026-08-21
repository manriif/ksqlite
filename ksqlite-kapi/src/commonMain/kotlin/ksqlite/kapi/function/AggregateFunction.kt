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
 * [Aggregate function](https://sqlite.org/appfunc.html#the_aggregate_function_callbacks).
 */
public interface AggregateFunction : Function {

    /**
     * Adds a row to the current window. [arguments], if any, correspond to the row being added.
     */
    public fun AggregateFunctionStepScope.step(arguments: Array<ProtectedValue>)

    /**
     * Returns the current value of the aggregate, determined by the contents of the current
     * window, and frees any resources allocated by earlier calls to [step]. The returned
     * [Result] must come from calling one of the receiver's result methods.
     */
    public fun AggregateFunctionFinalScope.final(): Result
}