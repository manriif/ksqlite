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

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests application-defined [ScalarFunction], [AggregateFunction] and [WindowFunction].
 */
class FunctionTest {

    @Test
    fun scalarFunctionWorks() = runSqliteConnectionTest { _, connection ->
        var destructorCalled = false
        var callCount = 0

        val function = object : ScalarFunction {

            override fun ScalarFunctionFuncScope.func(arguments: Array<ProtectedValue>) {
                callCount++
                setResult(arguments[0].getAsInt() + arguments[1].getAsInt())
            }

            override fun close() {
                destructorCalled = true
            }
        }

        connection.createFunction("add_ints", 2, SqliteTextEncoding.UTF8, function)

        connection.execute("CREATE TABLE numbers(a INTEGER, b INTEGER);")
        connection.execute("INSERT INTO numbers VALUES (1, 2), (10, 20);")

        val results = mutableListOf<Int>()

        connection.execute("SELECT add_ints(a, b) FROM numbers ORDER BY a;") { _, values, _ ->
            results.add(assertNotNull(values[0]).toInt())
            false
        }

        assertEquals(listOf(3, 30), results)
        assertEquals(2, callCount)

        connection.deleteFunction("add_ints", 2, SqliteTextEncoding.UTF8)
        assertTrue(destructorCalled)
    }

    @Test
    fun scalarFunctionAuxDataWorks() = runSqliteConnectionTest { _, connection ->
        // Auxiliary data is only preserved across invocations of the same call-site when the
        // argument it is keyed on is a constant across the statement (e.g. a literal), not a
        // per-row column value: see https://sqlite.org/c3ref/get_auxdata.html.
        var computeCount = 0
        var destroyCount = 0

        class Multiplier(val value: Int) : AutoCloseable {
            override fun close() {
                destroyCount++
            }
        }

        connection.createFunction("scaled", 2, SqliteTextEncoding.UTF8) { arguments ->
            val multiplier = getOrCreateAuxData(1) {
                computeCount++
                Multiplier(arguments[1].getAsInt())
            }

            setResult(arguments[0].getAsInt() * multiplier.value)
        }

        connection.execute("CREATE TABLE numbers(a INTEGER);")
        connection.execute("INSERT INTO numbers VALUES (1), (2), (3);")

        val results = mutableListOf<Int>()

        connection.execute("SELECT scaled(a, 2) FROM numbers;") { _, values, _ ->
            results.add(assertNotNull(values[0]).toInt())
            false
        }

        assertEquals(listOf(2, 4, 6), results)
        // The literal `2` argument is constant across all row evaluations of this call-site, so
        // its auxiliary data is computed once and reused.
        assertEquals(1, computeCount)

        connection.deleteFunction("scaled", 2, SqliteTextEncoding.UTF8)
        assertEquals(1, destroyCount)
    }

    @Test
    fun scalarFunctionErrorPropagates() = runSqliteConnectionTest { _, connection ->
        connection.createFunction("fail", 0, SqliteTextEncoding.UTF8) {
            setResultError("Something went wrong")
        }

        assertFailsWith<SQLiteException> {
            connection.execute("SELECT fail();")
        }
    }

    @Test
    fun aggregateFunctionWorks() = runSqliteConnectionTest { _, connection ->
        val function = object : AggregateFunction {

            override fun AggregateFunctionStepScope.step(arguments: Array<ProtectedValue>) {
                val sum = getOrCreateAggregateContext { intArrayOf(0) }
                sum[0] += arguments[0].getAsInt()
            }

            override fun AggregateFunctionFinalScope.final() {
                setResult(getContextOrNull<IntArray>()?.get(0) ?: 0)
            }
        }

        connection.createFunction("total", 1, SqliteTextEncoding.UTF8, function)

        connection.execute("CREATE TABLE numbers(a INTEGER);")
        connection.execute("INSERT INTO numbers VALUES (1), (2), (3);")

        var result: Int? = null

        connection.execute("SELECT total(a) FROM numbers;") { _, values, _ ->
            result = assertNotNull(values[0]).toInt()
            false
        }

        assertEquals(6, result)
    }

    @Test
    fun aggregateFunctionWithNoRowsReturnsDefault() = runSqliteConnectionTest { _, connection ->
        val function = object : AggregateFunction {

            override fun AggregateFunctionStepScope.step(arguments: Array<ProtectedValue>) = Unit

            override fun AggregateFunctionFinalScope.final() {
                setResult(getContextOrNull<IntArray>()?.get(0) ?: -1)
            }
        }

        connection.createFunction("total_or_default", 1, SqliteTextEncoding.UTF8, function)

        connection.execute("CREATE TABLE numbers(a INTEGER);")

        var result: String? = "unset"

        connection.execute("SELECT total_or_default(a) FROM numbers;") { _, values, _ ->
            result = values[0]
            false
        }

        assertEquals("-1", result)
    }

    @Test
    fun windowFunctionWorks() = runSqliteConnectionTest { _, connection ->
        val function = object : WindowFunction {

            override fun AggregateFunctionStepScope.step(arguments: Array<ProtectedValue>) {
                val sum = getOrCreateAggregateContext { intArrayOf(0) }
                sum[0] += arguments[0].getAsInt()
            }

            override fun WindowFunctionInverseScope.inverse(arguments: Array<ProtectedValue>) {
                val sum = getAggregateContextOrNull<IntArray>()

                if (sum != null) {
                    sum[0] -= arguments[0].getAsInt()
                }
            }

            override fun AggregateFunctionFinalScope.final() {
                setResult(getContextOrNull<IntArray>()?.get(0) ?: 0)
            }

            override fun AggregateFunctionFinalScope.value() {
                setResult(getContextOrNull<IntArray>()?.get(0) ?: 0)
            }
        }

        connection.createFunction("running_total", 1, SqliteTextEncoding.UTF8, function)

        connection.execute("CREATE TABLE numbers(id INTEGER, a INTEGER);")
        connection.execute("INSERT INTO numbers VALUES (1, 1), (2, 2), (3, 3);")

        val results = mutableListOf<Int>()

        val sql = """
            SELECT running_total(a) OVER (
                ORDER BY id ROWS BETWEEN 1 PRECEDING AND CURRENT ROW
            ) FROM numbers ORDER BY id;
        """.trimIndent()

        connection.execute(sql) { _, values, _ ->
            results.add(assertNotNull(values[0]).toInt())
            false
        }

        assertEquals(listOf(1, 3, 5), results)
    }

    @Test
    fun deleteFunctionOnUnknownFunctionIsANoOp() = runSqliteConnectionTest { _, connection ->
        // Deleting a function that was never registered is not an error in SQLite.
        connection.deleteFunction("does_not_exist", 0, SqliteTextEncoding.UTF8)
    }
}
