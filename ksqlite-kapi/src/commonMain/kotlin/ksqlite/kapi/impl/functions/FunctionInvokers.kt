package ksqlite.kapi.impl.functions

import ksqlite.capi.callbacks.Sqlite3FunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3FunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3FunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3FunctionValueCallback
import ksqlite.kapi.SQLiteValue.Companion.toSQLiteValues
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.AggregateFunctionFinalScope
import ksqlite.kapi.functions.AggregateFunctionStepScope
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.ScalarFunctionFuncScope
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.impl.runCatchingSQLiteException

/**
 * Invokes [AggregateFunction.step] and [WindowFunction.step].
 */
internal val ScalarFunctionFuncInvoker =
    Sqlite3FunctionFuncCallback { appData: ScalarFunction, context, arguments ->
        FunctionResultScopeImpl(context).use { scope ->
            appData.runCatchingSQLiteException(scope::handleError) {
                ScalarFunctionFuncScope(scope).func(arguments.toSQLiteValues())
            }
        }
    }

/**
 * Invokes [AggregateFunction.step] and [WindowFunction.step].
 */
internal val AggregateFunctionStepInvoker =
    Sqlite3FunctionStepCallback { appData: AggregateFunction, context, arguments ->
        FunctionScopeImpl(context).use { scope ->
            appData.runCatchingSQLiteException(scope::handleError) {
                AggregateFunctionStepScope(scope).step(arguments.toSQLiteValues())
            }
        }
    }

/**
 * Invokes [AggregateFunction.final] and [WindowFunction.final].
 */
internal val AggregateFunctionFinalInvoker =
    Sqlite3FunctionFinalCallback { appData: AggregateFunction, context ->
        FunctionResultScopeImpl(context).use { scope ->
            appData.runCatchingSQLiteException(scope::handleError) {
                AggregateFunctionFinalScope(scope).final()
            }
        }
    }

/**
 * Invokes [WindowFunction.inverse].
 */
internal val WindowFunctionInverseInvoker =
    Sqlite3FunctionInverseCallback { appData: WindowFunction, context, arguments ->
        FunctionScopeImpl(context).use { scope ->
            appData.runCatchingSQLiteException(scope::handleError) {
                AggregateFunctionStepScope(scope).inverse(arguments.toSQLiteValues())
            }
        }
    }

/**
 * Invokes [WindowFunction.value].
 */
internal val WindowFunctionValueInvoker =
    Sqlite3FunctionValueCallback { appData: WindowFunction, context ->
        FunctionResultScopeImpl(context).use { scope ->
            appData.runCatchingSQLiteException(scope::handleError) {
                AggregateFunctionFinalScope(scope).value()
            }
        }
    }