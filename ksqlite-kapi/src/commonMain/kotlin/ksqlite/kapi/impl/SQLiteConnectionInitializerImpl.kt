package ksqlite.kapi.impl

import ksqlite.capi.sqlite3_create_function_v2
import ksqlite.capi.sqlite3_create_window_function
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.SQLiteConnectionInitializer
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.impl.functions.AggregateFunctionFinalInvoker
import ksqlite.kapi.impl.functions.AggregateFunctionStepInvoker
import ksqlite.kapi.impl.functions.FunctionDestructor
import ksqlite.kapi.impl.functions.ScalarFunctionFuncInvoker
import ksqlite.kapi.impl.functions.WindowFunctionInverseInvoker
import ksqlite.kapi.impl.functions.WindowFunctionValueInvoker

internal class SQLiteConnectionInitializerImpl(private val db: sqlite3): SQLiteConnectionInitializer {

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: ScalarFunction
    ) = sqliteResultCheck(
        result = sqlite3_create_function_v2(
            db = db,
            name = name,
            nArg = argumentCount,
            encoding = encoding,
            appData = function,
            func = ScalarFunctionFuncInvoker,
            step = null,
            final = null,
            destroy = FunctionDestructor
        ),
        getDb = this::db
    )

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: AggregateFunction
    ) = sqliteResultCheck(
        result = sqlite3_create_function_v2(
            db = db,
            name = name,
            nArg = argumentCount,
            encoding = encoding,
            appData = function,
            func = null,
            step = AggregateFunctionStepInvoker,
            final = AggregateFunctionFinalInvoker,
            destroy = FunctionDestructor
        ),
        getDb = this::db
    )

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: WindowFunction
    ) = sqliteResultCheck(
        result = sqlite3_create_window_function(
            db = db,
            name = name,
            nArg = argumentCount,
            encoding = encoding,
            appData = function,
            step = AggregateFunctionStepInvoker,
            final = AggregateFunctionFinalInvoker,
            inverse = WindowFunctionInverseInvoker,
            value = WindowFunctionValueInvoker,
            destroy = FunctionDestructor
        ),
        getDb = this::db
    )
}