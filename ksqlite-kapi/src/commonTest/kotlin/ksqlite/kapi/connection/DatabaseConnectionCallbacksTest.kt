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
package ksqlite.kapi.connection

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.kapi.statement.PreparedStatementBase
import ksqlite.types.SqliteActionCode
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.SqliteTraceEventCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the callback/hook related APIs of [DatabaseConnection].
 */
class DatabaseConnectionCallbacksTest {

    @Test
    fun authorizerWorks() = runSqliteConnectionTest { _, connection ->
        val deniedActions = mutableListOf<SqliteActionCode>()

        connection.setAuthorizer { action, _, _, _, _ ->
            if (action == DROP_TABLE) {
                deniedActions.add(action)
                DENY
            } else {
                OK
            }
        }

        connection.execute("CREATE TABLE fruits(id INTEGER);")

        assertFailsWith<SQLiteException> {
            connection.execute("DROP TABLE fruits;")
        }

        assertEquals(1, deniedActions.size)
        assertEquals(DROP_TABLE, deniedActions[0])

        connection.setAuthorizer(null)
        connection.execute("DROP TABLE fruits;")
    }

    @Test
    fun busyHandlerWorks() = runSqliteConnectionTest { _, connection ->
        var callCount = 0

        connection.setBusyHandler { count ->
            callCount = count + 1
            0
        }

        // No contention is expected on this single, in-process connection, so the handler is
        // simply verified to be settable without error; it exercises setBusyHandler(null) too.
        connection.setBusyHandler(null)
        assertEquals(0, callCount)
    }

    @Test
    fun collationWorks() = runSqliteConnectionTest { _, connection ->
        var callCount = 0

        connection.createCollation("REVERSE", SqliteTextEncoding.UTF8) { lhs, rhs ->
            callCount++
            // Reverse the natural ordering.
            -lhs.decodeToString().compareTo(rhs.decodeToString())
        }

        connection.execute("CREATE TABLE fruits(name TEXT);")
        connection.execute("INSERT INTO fruits VALUES ('Apple'), ('Banana');")

        val names = mutableListOf<String>()

        connection.execute("SELECT name FROM fruits ORDER BY name COLLATE REVERSE;") { _, values, _ ->
            values[0]?.let(names::add)
            false
        }

        assertEquals(listOf("Banana", "Apple"), names)
        assertTrue(callCount > 0)

        connection.deleteCollation("REVERSE", SqliteTextEncoding.UTF8)

        assertFailsWith<SQLiteException> {
            connection.execute("SELECT name FROM fruits ORDER BY name COLLATE REVERSE;")
        }
    }

    @Test
    fun collationNeededWorks() = runSqliteConnectionTest { _, connection ->
        var requestedName: String? = null

        connection.setCollationNeeded { requestingConnection, _, name ->
            requestedName = name

            requestingConnection.createCollation(name, SqliteTextEncoding.UTF8) { lhs, rhs ->
                lhs.decodeToString().compareTo(rhs.decodeToString())
            }
        }

        connection.execute("CREATE TABLE fruits(name TEXT);")
        connection.execute("SELECT name FROM fruits ORDER BY name COLLATE dynamic_collation;")

        assertEquals("dynamic_collation", requestedName)
    }

    @Test
    fun commitAndRollbackHooksWork() = runSqliteConnectionTest { _, connection ->
        var commitCount = 0
        var rollbackCount = 0

        connection.setCommitHook {
            commitCount++
            false
        }

        connection.setRollbackHook {
            rollbackCount++
        }

        connection.execute("CREATE TABLE fruits(id INTEGER);")
        assertEquals(1, commitCount)

        connection.execute("BEGIN;")
        connection.execute("INSERT INTO fruits VALUES (1);")
        connection.execute("ROLLBACK;")
        assertEquals(1, rollbackCount)

        connection.setCommitHook(null)
        connection.setRollbackHook(null)

        connection.execute("INSERT INTO fruits VALUES (2);")
        assertEquals(1, commitCount)
    }

    @Test
    fun commitHookCanForceRollback() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")

        connection.setCommitHook { true }

        assertFailsWith<SQLiteException> {
            connection.execute("INSERT INTO fruits VALUES (1);")
        }

        connection.setCommitHook(null)

        var count = 0

        connection.execute("SELECT * FROM fruits;") { _, _, _ ->
            count++
            false
        }

        assertEquals(0, count)
    }

    @Test
    fun updateHookWorks() = runSqliteConnectionTest { _, connection ->
        val events = mutableListOf<Pair<SqliteActionCode.RowChange, String>>()

        connection.setUpdateHook { action, _, tableName, _ ->
            events.add(action to tableName)
        }

        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY);")
        connection.execute("INSERT INTO fruits VALUES (1);")
        connection.execute("UPDATE fruits SET id = 2 WHERE id = 1;")
        // A WHERE clause is required: a bare "DELETE FROM fruits;" hits SQLite's truncate
        // optimization, which skips the update hook entirely.
        connection.execute("DELETE FROM fruits WHERE id = 2;")

        assertEquals(3, events.size)
        assertEquals(INSERT, events[0].first)
        assertEquals(UPDATE, events[1].first)
        assertEquals(DELETE, events[2].first)
        assertTrue(events.all { it.second == "fruits" })

        connection.setUpdateHook(null)
    }

    @Test
    fun preupdateHookWorks() = runSqliteConnectionTest { _, connection ->
        var oldValue: Int? = null
        var newValue: Int? = null

        connection.setPreupdateHook { _, action, _, tableName, _, _ ->
            assertEquals("fruits", tableName)

            when (action) {
                INSERT -> newValue = newValue(0).getAsInt()
                UPDATE -> {
                    oldValue = oldValue(0).getAsInt()
                    newValue = newValue(0).getAsInt()
                }

                else -> Unit
            }
        }

        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1);")
        assertEquals(1, newValue)

        connection.execute("UPDATE fruits SET id = 2;")
        assertEquals(1, oldValue)
        assertEquals(2, newValue)

        connection.setPreupdateHook(null)
    }

    @Test
    fun progressHandlerCanInterruptExecution() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        repeat(1_000) { connection.execute("INSERT INTO fruits VALUES ($it);") }

        var callCount = 0

        connection.setProgressHandler(1) {
            callCount++
            callCount > 3
        }

        assertFailsWith<SQLiteException> {
            connection.execute("SELECT count(*) FROM fruits;")
        }

        assertTrue(callCount > 3)

        connection.setProgressHandler(0, null)
    }

    @Test
    fun traceWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")

        val events = mutableListOf<TraceEvent>()
        val seenRowValues = mutableListOf<Int>()

        connection.setTrace(SqliteTraceEventCode.STMT or ROW or PROFILE) { event ->
            events.add(event)

            when (event) {
                is Stmt -> {
                    assertTrue(event.statement.sql.contains("fruits"))
                    assertEquals(connection, event.statement.connection)
                    assertTrue(event.statement.columnCount >= 0)
                    // Must simply be readable without throwing.
                    val _ = event.statement.isReadOnly
                    val _ = event.statement.isBusy
                }

                is Row -> {
                    assertEquals(1, event.row.dataCount)
                    seenRowValues.add(event.row.getInt(0))
                }

                is Profile -> assertTrue(event.statement.sql.contains("fruits"))
                is Close -> Unit
            }
        }

        connection.execute("INSERT INTO fruits VALUES (1);")
        connection.execute("SELECT id FROM fruits;") { _, _, _ -> false }

        assertTrue(events.any { it is Stmt })
        assertTrue(events.any { it is Row })
        assertTrue(events.any { it is Profile })
        assertEquals(listOf(1), seenRowValues)

        connection.setTrace(null, null)
    }

    @Test
    fun tracedStatementAndRowAreStaleOnceCallbackReturns() =
        runSqliteConnectionTest { _, connection ->
            connection.execute("CREATE TABLE fruits(id INTEGER);")
            connection.execute("INSERT INTO fruits VALUES (1);")

            var capturedStatement: PreparedStatementBase? = null
            var capturedRow: ksqlite.kapi.statement.Row? = null

            connection.setTrace(SqliteTraceEventCode.STMT or ROW) { event ->
                when (event) {
                    is Stmt -> capturedStatement = event.statement
                    is TraceEvent.Row -> capturedRow = event.row
                    else -> Unit
                }
            }

            connection.execute("SELECT id FROM fruits;") { _, _, _ -> false }
            connection.setTrace(null, null)

            assertFailsWith<IllegalStateException> { assertNotNull(capturedStatement).sql }
            assertFailsWith<IllegalStateException> { assertNotNull(capturedRow).getInt(0) }
        }

    @Test
    fun autovacuumPagesWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("PRAGMA auto_vacuum = FULL;")
        connection.execute("CREATE TABLE fruits(id INTEGER);")

        var called = false

        connection.setAutovacuumPages { _, _, _, _ ->
            called = true
            0u
        }

        connection.execute("INSERT INTO fruits VALUES (1);")
        connection.execute("DELETE FROM fruits;")
        connection.execute("VACUUM;")

        assertTrue(called)

        connection.setAutovacuumPages(null)
    }
}
