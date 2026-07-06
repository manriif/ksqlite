package ksqlite.capi.vtab

import ksqlite.capi.runSqliteConnectionTest
import ksqlite.capi.sqlite3_create_module_v2
import ksqlite.capi.sqlite3_exec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the virtual table.
 * These tests were generated with Claude help.
 */
class VtabTest {

    @Test
    fun constraintPushdownWorks() = runSqliteConnectionTest { db ->
        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)

        val createModuleResult = sqlite3_create_module_v2(db, "kvtab", module, 0, null)
        assertEquals(OK, createModuleResult)

        val createVtabSql = "CREATE VIRTUAL TABLE t USING kvtab();"
        val createVTabResult = sqlite3_exec(db, createVtabSql, null, null, null)
        assertEquals(OK, createVTabResult)

        /*val insertSql = "INSERT INTO t(id, name) VALUES (1, 'Mangue'), (2, 'Melon');"
        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)*/

        var result: String? = null
        val selectSql = "SELECT name FROM t WHERE id = 2;"

        val selectResult = sqlite3_exec(db, selectSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            result = values[0]
            0
        }

        assertEquals(OK, selectResult)
        assertEquals("Melon", result)

        // xBestIndex was offered the "id = 2" constraint
        assertEquals(1, recorder.lastConstraintCount)

        // sanity: one filter pass for the point lookup
        assertEquals(1, recorder.rowidCallCount.let { recorder.filterCallCount })
    }

    @Test
    fun updateAndConflictWorks() = runSqliteConnectionTest { db ->
        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)

        val createModuleResult = sqlite3_create_module_v2(db, "kvtab", module, 0, null)
        assertEquals(OK, createModuleResult)

        val createVtabSql = "CREATE VIRTUAL TABLE t USING kvtab();";
        val createVtabResult = sqlite3_exec(db, createVtabSql, null, null, null);
        assertEquals(OK, createVtabResult)

        val insertSql = "INSERT INTO t (id, name) VALUES (1, 'Mangue');"
        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)
        assertEquals(1, recorder.updateCallCount)
        assertEquals(ABORT, recorder.lastConflictMode)

        // Only `id` is assigned here, so `name` should be read back as "unchanged".
        val nochangeCountBefore = recorder.nochangeSeenCount
        val updateSql = "UPDATE t SET id = 1 WHERE id = 1;"
        val updateResult = sqlite3_exec(db, updateSql, null, null, null)
        assertEquals(OK, updateResult)
        assertTrue(recorder.nochangeSeenCount > nochangeCountBefore)

        // Conflicting insert resolved with IGNORE should surface via sqlite3_vtab_on_conflict.
        val insertOrIgnoreSql = "INSERT OR IGNORE INTO t (id, name) VALUES (1, 'Duplicate');"
        val insertOrIgnoreResult = sqlite3_exec(db, insertOrIgnoreSql, null, null, null)
        assertEquals(OK, insertOrIgnoreResult)
        assertEquals(IGNORE, recorder.lastConflictMode)

        val deleteCountBefore = recorder.updateCallCount
        val deleteSql = "DELETE FROM t WHERE id = 1;"
        val deleteResult = sqlite3_exec(db, deleteSql, null, null, null)
        assertEquals(OK, deleteResult)
        assertTrue(recorder.updateCallCount > deleteCountBefore)

        var rowCount = -1L
        val selectSql = "SELECT COUNT(*) FROM t;"

        val selectResult = sqlite3_exec(db, selectSql, null, null) { _, _, values, _ ->
            rowCount = assertNotNull(values[0]).toLong()
            0
        }

        assertEquals(OK, selectResult)
        assertEquals(0L, rowCount)
    }

    @Test
    fun virtualTableTransactionWorks() = runSqliteConnectionTest { db ->
        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)

        assertEquals(OK, sqlite3_create_module_v2(db, "kvtab", module, 0, null))
        assertEquals(
            OK,
            sqlite3_exec(db, "CREATE VIRTUAL TABLE t USING kvtab();", null, null, null)
        )

        // Even a single autocommit write is wrapped in its own vtab transaction.
        assertEquals(
            OK,
            sqlite3_exec(db, "INSERT INTO t (id, name) VALUES (1, 'Mangue');", null, null, null)
        )
        assertEquals(1, recorder.beginCallCount)
        assertEquals(1, recorder.syncCallCount)
        assertEquals(1, recorder.commitCallCount)
        assertEquals(0, recorder.rollbackCallCount)

        // An explicit transaction that's rolled back should invoke xRollback instead of xCommit.
        assertEquals(OK, sqlite3_exec(db, "BEGIN;", null, null, null))
        assertEquals(
            OK,
            sqlite3_exec(db, "INSERT INTO t (id, name) VALUES (2, 'Melon');", null, null, null)
        )
        assertEquals(OK, sqlite3_exec(db, "ROLLBACK;", null, null, null))

        assertEquals(2, recorder.beginCallCount)
        assertEquals(1, recorder.commitCallCount)
        assertEquals(1, recorder.rollbackCallCount)

        var count = -1L
        assertEquals(
            OK,
            sqlite3_exec(db, "SELECT COUNT(*) FROM t;", null, null) { _, _, values, _ ->
                count = assertNotNull(values[0]).toLong()
                0
            },
        )
        assertEquals(1L, count) // only the committed row survives
    }

    @Test
    fun virtualTableSavepointWorks() = runSqliteConnectionTest { db ->
        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)

        assertEquals(OK, sqlite3_create_module_v2(db, "kvtab", module, 0, null))
        assertEquals(
            OK,
            sqlite3_exec(db, "CREATE VIRTUAL TABLE t USING kvtab();", null, null, null)
        )
        assertEquals(OK, sqlite3_exec(db, "BEGIN;", null, null, null))
        assertEquals(
            OK,
            sqlite3_exec(db, "INSERT INTO t (id, name) VALUES (1, 'Mangue');", null, null, null)
        )
        assertEquals(OK, sqlite3_exec(db, "SAVEPOINT sp1;", null, null, null))
        assertEquals(
            OK,
            sqlite3_exec(db, "INSERT INTO t (id, name) VALUES (2, 'Melon');", null, null, null)
        )
        assertEquals(OK, sqlite3_exec(db, "ROLLBACK TO sp1;", null, null, null))
        assertEquals(OK, sqlite3_exec(db, "RELEASE sp1;", null, null, null))
        assertEquals(OK, sqlite3_exec(db, "COMMIT;", null, null, null))

        assertEquals(1, recorder.savepointCalls.size)
        assertEquals(1, recorder.rollbackToCalls.size)
        assertEquals(1, recorder.releaseCalls.size)
        assertEquals(2, recorder.commitCallCount)
        assertEquals(0, recorder.rollbackCallCount)

        var count = -1L

        assertEquals(
            OK,
            sqlite3_exec(db, "SELECT COUNT(*) FROM t;", null, null) { _, _, values, _ ->
                count = assertNotNull(values[0]).toLong()
                0
            },
        )

        assertEquals(1L, count) // row 2 was undone by ROLLBACK TO sp1, row 1 survived the commit
    }

    @Test
    fun virtualTableRenameWorks() = runSqliteConnectionTest { db ->
        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)

        assertEquals(OK, sqlite3_create_module_v2(db, "kvtab", module, 0, null))
        assertEquals(
            OK,
            sqlite3_exec(db, "CREATE VIRTUAL TABLE t USING kvtab();", null, null, null)
        )

        assertFalse(recorder.renameCalled)
        assertEquals(OK, sqlite3_exec(db, "ALTER TABLE t RENAME TO t_renamed;", null, null, null))
        assertTrue(recorder.renameCalled)

        assertEquals(OK, sqlite3_exec(db, "SELECT COUNT(*) FROM t_renamed;", null, null, null))
    }

    @Test
    fun virtualTableIntegrityFindFunctionAndLifecycleWork() = runSqliteConnectionTest { db ->
        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)

        assertEquals(OK, sqlite3_create_module_v2(db, "kvtab", module, 0, null))
        assertEquals(
            OK,
            sqlite3_exec(db, "CREATE VIRTUAL TABLE t1 USING kvtab();", null, null, null)
        )
        assertEquals(
            OK,
            sqlite3_exec(db, "CREATE VIRTUAL TABLE t2 USING kvtab();", null, null, null)
        )
        assertEquals(
            OK,
            sqlite3_exec(db, "INSERT INTO t1 (id, name) VALUES (1, 'Mangue');", null, null, null)
        )

        // xFindFunction: SQLite offers the vtab a chance to overload functions applied to its
        // own columns, such as `like` used against a TEXT column here.
        val findFunctionCountBefore = recorder.findFunctionCallCount
        assertEquals(
            OK,
            sqlite3_exec(db, "SELECT * FROM t1 WHERE name LIKE 'M%';", null, null, null)
        )
        assertTrue(recorder.findFunctionCallCount > findFunctionCountBefore)

        // xIntegrity
        assertEquals(0, recorder.integrityCallCount)
        assertEquals(OK, sqlite3_exec(db, "PRAGMA integrity_check;", null, null, null))
        assertTrue(recorder.integrityCallCount >= 1)

        // xDestroy: dropping a virtual table destroys its (here, in-memory) persistent state.
        assertEquals(0, recorder.destroyCallCount)
        assertEquals(OK, sqlite3_exec(db, "DROP TABLE t1;", null, null, null))
        assertEquals(1, recorder.destroyCallCount)

        // xDisconnect: unregistering the module while t2 is still alive releases it without
        // destroying anything.
        assertEquals(0, recorder.disconnectCallCount)
        assertEquals(OK, sqlite3_create_module_v2(db, "kvtab", null, null, null))
        assertEquals(1, recorder.disconnectCallCount)
    }
}