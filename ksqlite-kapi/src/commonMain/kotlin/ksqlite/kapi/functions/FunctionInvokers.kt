package ksqlite.kapi.functions

import ksqlite.capi.callbacks.Sqlite3FunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3FunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3FunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3FunctionValueCallback
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
internal val ScalarFunctionFuncInvoker =
    Sqlite3FunctionFuncCallback { appData: ScalarFunction, context, arguments ->
        appData.scoped(context) { scope ->
            ScalarFunctionFuncScope(scope).func(arguments.toProtectedValues(scope))
        }
    }

/**
 * Invokes [AggregateFunction.step] and [WindowFunction.step].
 */
internal val AggregateFunctionStepInvoker =
    Sqlite3FunctionStepCallback { appData: AggregateFunction, context, arguments ->
        appData.scoped(context) { scope ->
            AggregateFunctionStepScope(scope).step(arguments.toProtectedValues(scope))
        }
    }

/**
 * Invokes [AggregateFunction.final] and [WindowFunction.final].
 */
internal val AggregateFunctionFinalInvoker =
    Sqlite3FunctionFinalCallback { appData: AggregateFunction, context ->
        appData.scoped(context) { scope ->
            AggregateFunctionFinalScope(scope).final()
        }
    }

/**
 * Invokes [WindowFunction.inverse].
 */
internal val WindowFunctionInverseInvoker =
    Sqlite3FunctionInverseCallback { appData: WindowFunction, context, arguments ->
        appData.scoped(context) { scope ->
            AggregateFunctionStepScope(scope).inverse(arguments.toProtectedValues(scope))
        }
    }

/**
 * Invokes [WindowFunction.value].
 */
internal val WindowFunctionValueInvoker =
    Sqlite3FunctionValueCallback { appData: WindowFunction, context ->
        appData.scoped(context) { scope ->
            AggregateFunctionFinalScope(scope).value()
        }
    }