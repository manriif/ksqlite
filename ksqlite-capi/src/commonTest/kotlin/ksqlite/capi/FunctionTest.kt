package ksqlite.capi

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
        var destructorCalled = false
        var funcCalled = false
        val encoding = SqliteTextEncoding.UTF8

        val createResult = sqlite3_create_function_v2(
            db = db,
            name = "pow2",
            nArg = 1,
            encoding = encoding,
            appData = 50000,
            step = null,
            final = null,
            func = { appData, context, values ->
                assertEquals(50000, appData)
                assertEquals(1, values.size)

                val value = values[0]
                val number = sqlite3_value_double(value)
                val result = number.pow(2)

                sqlite3_result_double(context, result)
                funcCalled = true
            },
            destroy = { appData ->
                assertEquals(50000, appData)
                destructorCalled = true
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

        assertEquals(OK, execResult)
        assertTrue(funcCalled)
        assertEquals(16.0, assertNotNull(result), .0)

        val deleteResult = sqlite3_create_function(
            db = db,
            name = "pow2",
            nArg = 1,
            encoding = encoding,
            appData = null,
            func = null,
            step = null,
            final = null
        )

        assertEquals(OK, deleteResult)
        assertTrue(destructorCalled)
    }

    private class JoinToString(
        val separator: String,
        val prefix: String,
        val postfix: String
    ) {
        val fruits = mutableListOf<String>()
    }

    @Test
    fun aggregateFunctionWorks() = runSqliteConnectionTest { db ->
        var destructorCalled = false
        var stepCalledCount = 0
        var finalCalled = false
        var aggregateContextFactoryCallCount = 0
        val encoding = SqliteTextEncoding.UTF8

        val createResult = sqlite3_create_function_v2(
            db = db,
            name = "joinToString",
            nArg = 4,
            encoding = encoding,
            appData = 12132,
            func = null,
            step = { appData, context, values ->
                assertEquals(12132, appData)

                val ctx = assertNotNull(sqlite3_aggregate_context<JoinToString>(context) {
                    assertEquals(4, values.size)
                    aggregateContextFactoryCallCount++

                    JoinToString(
                        separator = assertNotNull(sqlite3_value_text(values[1])),
                        prefix = assertNotNull(sqlite3_value_text(values[2])),
                        postfix = assertNotNull(sqlite3_value_text(values[3]))
                    )
                })

                ctx.fruits.add(assertNotNull(sqlite3_value_text(values[0])))
                stepCalledCount++
            },
            final = { appData, context ->
                assertEquals(12132, appData)
                val ctx = assertNotNull(sqlite3_aggregate_context<JoinToString>(context, null))

                val result = ctx.run {
                    fruits.joinToString(separator, prefix, postfix)
                }

                sqlite3_result_text(context, result)
                finalCalled = true
            },
            destroy = { appData ->
                assertEquals(12132, appData)
                destructorCalled = true
            },
        )

        assertEquals(OK, createResult)

        val insertSql = """
            CREATE TABLE fruits (name TEXT NOT NULL);
            INSERT INTO fruits VALUES ('Mangue'), ('Melon'), ('Pastèque'), ('Orange'); 
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        val selectSql = "SELECT joinToString(name, '_', '[(', ')]') FROM fruits;"
        var result: String? = null

        val selectResult = sqlite3_exec(db, selectSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            result = values[0]
            0
        }

        assertEquals(OK, selectResult)
        assertEquals(1, aggregateContextFactoryCallCount)
        assertEquals(4, stepCalledCount)
        assertTrue(finalCalled)

        val expectedResult = "[(Mangue_Melon_Pastèque_Orange)]"
        assertEquals(expectedResult, result)

        val deleteResult = sqlite3_create_function(
            db = db,
            name = "joinToString",
            nArg = 4,
            encoding = encoding,
            appData = null,
            func = null,
            step = null,
            final = null
        )

        assertEquals(OK, deleteResult)
        assertTrue(destructorCalled)
    }

    private class WindowSum {
        var sum: Long = 0L
    }

    @Test
    fun windowFunctionWorks() = runSqliteConnectionTest { db ->
        var destructorCalled = false
        var stepCalledCount = 0
        var inverseCalledCount = 0
        var valueCalledCount = 0
        var finalCalled = false
        var aggregateContextFactoryCallCount = 0
        val encoding = SqliteTextEncoding.UTF8

        val createResult = sqlite3_create_window_function(
            db = db,
            name = "wsum",
            nArg = 1,
            encoding = encoding,
            appData = 424242,
            step = { appData, context, values ->
                assertEquals(424242, appData)

                val ctx = assertNotNull(sqlite3_aggregate_context<WindowSum>(context) {
                    assertEquals(1, values.size)
                    aggregateContextFactoryCallCount++
                    WindowSum()
                })

                ctx.sum += sqlite3_value_int64(values[0])
                stepCalledCount++
            },
            inverse = { appData, context, values ->
                assertEquals(424242, appData)

                // xInverse is only ever called after xStep has run at least once,
                // so the context is guaranteed to already exist here
                val ctx = assertNotNull(sqlite3_aggregate_context<WindowSum>(context, null))
                ctx.sum -= sqlite3_value_int64(values[0])
                inverseCalledCount++
            },
            value = { appData, context ->
                assertEquals(424242, appData)
                val ctx = assertNotNull(sqlite3_aggregate_context<WindowSum>(context, null))
                sqlite3_result_int64(context, ctx.sum)
                valueCalledCount++
            },
            final = { appData, context ->
                assertEquals(424242, appData)
                val ctx = assertNotNull(sqlite3_aggregate_context<WindowSum>(context, null))
                sqlite3_result_int64(context, ctx.sum)
                finalCalled = true
            },
            destroy = { appData ->
                assertEquals(424242, appData)
                destructorCalled = true
            },
        )

        assertEquals(OK, createResult)

        val insertSql = """
            CREATE TABLE numbers(id INTEGER NOT NULL, val INTEGER NOT NULL);
            INSERT INTO numbers VALUES (1, 10), (2, 20), (3, 30), (4, 40), (5, 50);
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        // ROWS BETWEEN 1 PRECEDING AND CURRENT ROW forces SQLite to slide the frame,
        // which is what actually exercises xInverse/xValue instead of falling back
        // to a plain one-shot aggregate.
        val selectSql = """
            SELECT wsum(val) OVER (ORDER BY id ROWS BETWEEN 1 PRECEDING AND CURRENT ROW)
            FROM numbers ORDER BY id;
        """.trimIndent()

        val results = mutableListOf<Long>()

        val selectResult = sqlite3_exec(db, selectSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            results.add(assertNotNull(values[0]).toLong())
            0
        }

        assertEquals(OK, selectResult)

        val expectedResults = listOf(10L, 30L, 50L, 70L, 90L)
        assertEquals(expectedResults, results)

        assertEquals(1, aggregateContextFactoryCallCount)
        assertEquals(5, stepCalledCount)
        assertEquals(3, inverseCalledCount)
        assertEquals(5, valueCalledCount)
        assertTrue(finalCalled)

        val deleteResult = sqlite3_create_window_function(
            db = db,
            name = "wsum",
            nArg = 1,
            encoding = encoding,
            appData = null,
            step = null,
            final = null,
            value = null,
            inverse = null,
            destroy = null
        )

        assertEquals(OK, deleteResult)
        assertTrue(destructorCalled)
    }

    @Test
    fun valueWorks() = runSqliteConnectionTest {

        // TODO move
        //val contextDb = sqlite3_context_db_handle(context)
        //assertEquals(db, contextDb)

    }
}