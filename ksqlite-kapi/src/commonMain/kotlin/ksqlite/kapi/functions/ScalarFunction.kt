package ksqlite.kapi.functions

import ksqlite.kapi.SQLiteValue

/**
 * [Scalar Function](https://sqlite.org/appfunc.html#the_scalar_function_callback)
 */
public fun interface ScalarFunction : Function {

    /**
     * This method is invoked to handle a function call.
     */
    public fun ScalarFunctionFuncScope.func(arguments: Array<SQLiteValue?>)
}