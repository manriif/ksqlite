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

import ksqlite.capi.callbacks.SqliteFunctionFinalCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.callbacks.SqliteFunctionInverseCallback
import ksqlite.capi.callbacks.SqliteFunctionStepCallback
import ksqlite.capi.callbacks.SqliteFunctionValueCallback
import ksqlite.capi.sqlite3_context
import ksqlite.kapi.helpers.runCatchingSQLiteException
import ksqlite.kapi.value.toProtectedValues

private inline fun <F : Function> F.scoped(
    context: sqlite3_context,
    block: F.(scope: FunctionScopeImpl) -> Unit
) {
    FunctionScopeImpl(context).use { scope ->
        runCatchingSQLiteException(scope::handleError) {
            block(scope)
        }
    }
}

/**
 * Invokes [AggregateFunction.step] and [WindowFunction.step].
 */
internal val ScalarFunctionFuncCallback =
    SqliteFunctionFuncCallback { appData: ScalarFunction, context, arguments ->
        appData.scoped(context) { scope ->
            ScalarFunctionFuncScope(scope).func(arguments.toProtectedValues(scope))
        }
    }

/**
 * Invokes [AggregateFunction.step] and [WindowFunction.step].
 */
internal val AggregateFunctionStepCallback =
    SqliteFunctionStepCallback { appData: AggregateFunction, context, arguments ->
        appData.scoped(context) { scope ->
            AggregateFunctionStepScope(scope).step(arguments.toProtectedValues(scope))
        }
    }

/**
 * Invokes [AggregateFunction.final] and [WindowFunction.final].
 */
internal val AggregateFunctionFinalCallback =
    SqliteFunctionFinalCallback { appData: AggregateFunction, context ->
        appData.scoped(context) { scope ->
            AggregateFunctionFinalScope(scope).final()
        }
    }

/**
 * Invokes [WindowFunction.inverse].
 */
internal val WindowFunctionInverseCallback =
    SqliteFunctionInverseCallback { appData: WindowFunction, context, arguments ->
        appData.scoped(context) { scope ->
            WindowFunctionInverseScope(scope).inverse(arguments.toProtectedValues(scope))
        }
    }

/**
 * Invokes [WindowFunction.value].
 */
internal val WindowFunctionValueCallback =
    SqliteFunctionValueCallback { appData: WindowFunction, context ->
        appData.scoped(context) { scope ->
            AggregateFunctionFinalScope(scope).value()
        }
    }