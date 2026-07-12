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