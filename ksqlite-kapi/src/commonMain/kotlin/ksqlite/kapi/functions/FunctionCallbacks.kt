package ksqlite.kapi.functions

import ksqlite.capi.callbacks.SqliteFunctionFinalCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.callbacks.SqliteFunctionInverseCallback
import ksqlite.capi.callbacks.SqliteFunctionStepCallback
import ksqlite.capi.callbacks.SqliteFunctionValueCallback
import ksqlite.capi.types.sqlite3_context
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
            AggregateFunctionStepScope(scope).inverse(arguments.toProtectedValues(scope))
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