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
package ksqlite.kapi.vtab

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.kapi.runSqliteTest
import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests [VirtualTableModule], [VirtualTable] and [VirtualTableCursor], via the eponymous
 * [KvVirtualTable] test double (see [createKvModule]), which implements the full optional
 * function surface (update, transactions, rename, savepoints, integrity, findFunction).
 */
class VirtualTableTest {

    /**
     * Registers a fresh [KvVirtualTable]-backed module named [moduleName] and creates an actual
     * (non-eponymous-access) virtual table [tableName] from it via `CREATE VIRTUAL TABLE`, which
     * is what SQLite requires before it will invoke transaction/rename hooks on the resulting
     * table.
     */
    private fun DatabaseConnection.createKvVtab(
        moduleName: String,
        tableName: String = "t"
    ): KvModuleRecorder {
        val recorder = createKvModule(moduleName)
        execute("CREATE VIRTUAL TABLE $tableName USING $moduleName();")
        return recorder
    }

    @Test
    fun connectAndFullScanWork() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvModule("kv_scan")

        // Directly querying an eponymous virtual table connects to it without a CREATE VIRTUAL
        // TABLE statement.
        val names = mutableMapOf<Long, String>()

        connection.execute("SELECT id, name FROM kv_scan;") { _, values, _ ->
            val id = requireNotNull(values[0]).toLong()
            names[id] = values[1].orEmpty()
            false
        }

        assertTrue(recorder.createOrConnectCallCount >= 1)
        assertEquals(0, names.size)
    }

    @Test
    fun bestIndexReceivesConstraints() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvModule("kv_bestindex")

        connection.execute("SELECT id FROM kv_bestindex WHERE id = 42;")

        assertEquals(1, recorder.lastConstraintCount)
    }

    @Test
    fun disconnectIsCalledWhenConnectionCloses() = runSqliteTest { sqlite ->
        val recorder = KvModuleRecorder()

        sqlite.open(":memory:").use { other ->
            val _ = other.createKvModule("kv_disconnect", recorder)
            other.execute("SELECT * FROM kv_disconnect;")
        }

        assertEquals(1, recorder.disconnectCallCount)
    }

    @Test
    fun closingARegisteredModuleRemovesIt() = runSqliteConnectionTest { _, connection ->
        val recorder = KvModuleRecorder()

        val module = object : VirtualTableModule.Eponymous() {
            override fun VirtualTableCreateOrConnectScope.connect(
                connection: DatabaseConnection,
                arguments: Array<String>
            ): VirtualTable {
                declare("CREATE TABLE x(id INTEGER PRIMARY KEY, name TEXT NOT NULL)")
                return KvVirtualTable(recorder)
            }
        }

        val registration = connection.createModule("kv_delete", module = module)
        connection.execute("SELECT * FROM kv_delete;")

        registration.close()

        assertFailsWith<SQLiteException> {
            connection.execute("SELECT * FROM kv_delete;")
        }
    }

    @Test
    fun cursorReadsEmptyTable() = runSqliteConnectionTest { _, connection ->
        // The KvVirtualTable's own row store starts empty, so a full scan over zero rows exercises
        // open()/filter()/eof()/close() end-to-end without depending on update() working.
        val _ = connection.createKvModule("kv_cursor")

        var rowCount = 0

        connection.execute("SELECT * FROM kv_cursor;") { _, _, _ ->
            rowCount++
            false
        }

        assertEquals(0, rowCount)
    }

    @Test
    fun updateAndConflictWorks() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvVtab("kv_update")

        connection.execute("INSERT INTO t (id, name) VALUES (1, 'Mangue');")
        assertEquals(1, recorder.updateCallCount)
        assertEquals(ABORT, recorder.lastConflictMode)

        // Only `id` is assigned here, so `name` should be read back as "unchanged".
        val nochangeCountBefore = recorder.nochangeSeenCount
        connection.execute("UPDATE t SET id = 1 WHERE id = 1;")
        assertTrue(recorder.nochangeSeenCount > nochangeCountBefore)

        // Conflicting insert resolved with IGNORE should surface via sqlite3_vtab_on_conflict.
        connection.execute("INSERT OR IGNORE INTO t (id, name) VALUES (1, 'Duplicate');")
        assertEquals(IGNORE, recorder.lastConflictMode)

        val deleteCountBefore = recorder.updateCallCount
        connection.execute("DELETE FROM t WHERE id = 1;")
        assertTrue(recorder.updateCallCount > deleteCountBefore)

        var rowCount = -1L

        connection.execute("SELECT COUNT(*) FROM t;") { _, values, _ ->
            rowCount = requireNotNull(values[0]).toLong()
            false
        }

        assertEquals(0L, rowCount)
    }

    @Test
    fun transactionHooksAreCalled() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvVtab("kv_txn")

        // Even a single autocommit write is wrapped in its own vtab transaction.
        connection.execute("INSERT INTO t (id, name) VALUES (1, 'Mangue');")

        assertEquals(1, recorder.beginCallCount)
        assertEquals(2, recorder.syncCallCount)
        assertEquals(2, recorder.commitCallCount)
        assertEquals(0, recorder.rollbackCallCount)

        // An explicit transaction that's rolled back should invoke xRollback instead of xCommit.
        connection.execute("BEGIN;")
        connection.execute("INSERT INTO t (id, name) VALUES (2, 'Melon');")
        connection.execute("ROLLBACK;")

        assertEquals(2, recorder.beginCallCount)
        assertEquals(2, recorder.commitCallCount)
        assertEquals(1, recorder.rollbackCallCount)

        var count = -1L

        connection.execute("SELECT COUNT(*) FROM t;") { _, values, _ ->
            count = requireNotNull(values[0]).toLong()
            false
        }

        // only the committed row survives
        assertEquals(1L, count)
    }

    @Test
    fun nestedTransactionSavepointHooksAreCalled() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvVtab("kv_savepoint")

        connection.execute("BEGIN;")
        connection.execute("INSERT INTO t (id, name) VALUES (1, 'Mangue');")
        connection.execute("SAVEPOINT sp1;")
        connection.execute("INSERT INTO t (id, name) VALUES (2, 'Melon');")
        connection.execute("ROLLBACK TO sp1;")
        connection.execute("RELEASE sp1;")
        connection.execute("COMMIT;")

        assertEquals(1, recorder.savepointCalls.size)
        assertEquals(1, recorder.rollbackToCalls.size)
        assertEquals(1, recorder.releaseCalls.size)
        assertEquals(2, recorder.commitCallCount)
        assertEquals(0, recorder.rollbackCallCount)

        var count = -1L

        connection.execute("SELECT COUNT(*) FROM t;") { _, values, _ ->
            count = requireNotNull(values[0]).toLong()
            false
        }

        // row 2 was undone by ROLLBACK TO sp1, row 1 survived the commit
        assertEquals(1L, count)
    }

    @Test
    fun renameIsCalled() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvVtab("kv_rename")
        assertFalse(recorder.renameCalled)

        connection.execute("ALTER TABLE t RENAME TO t_renamed;")
        assertTrue(recorder.renameCalled)

        connection.execute("SELECT COUNT(*) FROM t_renamed;")
    }

    @Test
    fun integrityCheckIsCalled() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvVtab("kv_integrity")
        assertEquals(0, recorder.integrityCallCount)

        connection.execute("PRAGMA integrity_check;")
        assertEquals(1, recorder.integrityCallCount)
    }

    @Test
    fun findFunctionOverloadIsUsed() = runSqliteConnectionTest { _, connection ->
        val recorder = connection.createKvVtab("kv_findfn")

        connection.execute("INSERT INTO t (id, name) VALUES (1, 'Mangue'), (2, 'Melon');")

        // A global "tag" function must exist for SQLite to resolve calls to it at all; xFindFunction
        // is only consulted afterward to let the vtab offer an overload for its own column.
        connection.createFunction("tag", 1, SqliteTextEncoding.UTF8) { arguments ->
            resultString("global:${requireNotNull(arguments[0].getAsString())}")
        }

        // Sanity check: without a vtab column argument, the global implementation runs unmodified.
        var globalValue: String? = null

        connection.execute("SELECT tag('x');") { _, values, _ ->
            globalValue = values[0]
            false
        }

        assertEquals("global:x", globalValue)
        assertEquals(0, recorder.overloadCallCount)

        // Applying "tag" to the vtab's own column should trigger xFindFunction and, since it
        // returns an overload, run the vtab's implementation instead of the global one.
        val taggedValues = mutableListOf<String>()

        connection.execute("SELECT tag(name) FROM t ORDER BY id;") { _, values, _ ->
            taggedValues.add(requireNotNull(values[0]))
            false
        }

        assertEquals(listOf("vtab:Mangue", "vtab:Melon"), taggedValues)
        assertTrue(recorder.findFunctionOfferedCount >= 1)
        // one call per row
        assertEquals(2, recorder.overloadCallCount)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed connection violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun createModuleFailsOnceConnectionClosed() = runSqliteConnectionTest { _, connection ->
        connection.close()

        assertFailsWith<IllegalStateException> {
            connection.createKvModule("kv_closed")
        }
    }

    @Test
    fun closingARegisteredModuleIsANoOpOnceConnectionClosed() = runSqliteConnectionTest { _, connection ->
        val module = object : VirtualTableModule.EponymousOnly() {
            override fun VirtualTableCreateOrConnectScope.connect(
                connection: DatabaseConnection,
                arguments: Array<String>
            ): VirtualTable {
                declare("CREATE TABLE x(id INTEGER PRIMARY KEY, name TEXT NOT NULL)")
                return KvVirtualTable(KvModuleRecorder())
            }
        }

        val registration = connection.createModule("kv_delete_closed", module = module)
        connection.close()

        // The connection already tore everything down, closing the handle afterward is a no-op
        // rather than a failure, the same as closing any other resource a second time.
        registration.close()
    }
}
