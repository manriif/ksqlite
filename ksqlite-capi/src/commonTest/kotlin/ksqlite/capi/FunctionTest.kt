package ksqlite.capi

import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteTextEncoding
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests application defined functions.
 */
class FunctionTest {

    @Test
    fun scalarFunctionWorks() = runSqliteConnectionTest { db ->
        var callbackCalled = true

        val createResult = sqlite3_create_function(
            db = db,
            name = "pow2",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = null,
            step = null,
            final = null,
            func = { _, context, values ->
                assertEquals(1, values.size)

                val value = values[0]
                val number = sqlite3_value_double(value)
                val result = number.pow(2)

                sqlite3_result_double(context, result)
                callbackCalled = true
            },
        )

        assertEquals(OK, createResult)

        val failSql = "SELECT pow2(2, 18);"
        val failResult = sqlite3_exec(db, failSql, null, null, null)
        assertEquals(ERROR, failResult)

        val sql = "SELECT pow2(4);"
        var result: Double? = null

        val execResult = sqlite3_exec(db, sql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            result = values[0]?.toDoubleOrNull()
            0
        }

        assertEquals(SqliteResultCode.OK, execResult)
        assertTrue(callbackCalled)
        assertEquals(16.0, assertNotNull(result), .0)
    }

    /*@Test
    fun aggregateFunctionWorks() = runSqliteConnectionTest { db ->
        var callbackCalled = true
        var counter = 0

        val createResult = sqlite3_create_function(
            db = db,
            name = "pow2",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = null,
            func = null,
            step = {

            },
            final = { _, context ->
                assertEquals(1, values.size)

                val value = values[0]
                val number = sqlite3_value_double(value)
                val result = number.pow(2)

                sqlite3_result_double(context, result)
                callbackCalled = true
            },
        )

        assertEquals(SqliteResultCode.OK, createResult)

        val failSql = "SELECT pow2(2, 18);"
        val failResult = sqlite3_exec(db, failSql, null, null, null)
        assertEquals(SqliteResultCode.ERROR, failResult)

        val sql = "SELECT pow2(4);"
        var result: Double? = null

        val execResult = sqlite3_exec(db, sql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            result = values[0]?.toDoubleOrNull()
            0
        }

        assertEquals(SqliteResultCode.OK, execResult)
        assertTrue(callbackCalled)
        assertEquals(16.0, assertNotNull(result), .0)
    }*/
}