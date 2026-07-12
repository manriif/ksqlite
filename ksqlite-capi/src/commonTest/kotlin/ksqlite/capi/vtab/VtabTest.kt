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
package ksqlite.capi.vtab

import ksqlite.capi.runSqliteConnectionTest
import ksqlite.capi.runSqliteTest
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_close
import ksqlite.capi.sqlite3_create_function
import ksqlite.capi.sqlite3_create_module
import ksqlite.capi.sqlite3_create_module_v2
import ksqlite.capi.sqlite3_drop_modules
import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.sqlite3_exec
import ksqlite.capi.sqlite3_open
import ksqlite.capi.sqlite3_result_text
import ksqlite.capi.sqlite3_value_text
import ksqlite.types.SqliteResultCode.OK
import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the virtual table.
 *
 * TODO: inject errors to test failure paths
 */
class VtabTest {

    @Test
    fun lifecycleWorks() = runSqliteTest {
        val outDb = sqlite3.OutputParam()
        val dbOpenResult = sqlite3_open(":memory:", outDb)
        assertEquals(OK, dbOpenResult)
        val db = assertNotNull(outDb.value)

        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)
        var appDataDestructorCalled = false

        val createModuleResult = sqlite3_create_module_v2(db, "kvtab", module, 1_001) { appData ->
            assertEquals(1_001, appData)
            appDataDestructorCalled = true
        }

        assertEquals(OK, createModuleResult)

        val createVtab0Sql = "CREATE VIRTUAL TABLE t0 USING kvtab(657);"
        val createVtab0Result = sqlite3_exec(db, createVtab0Sql, null, null, null)
        assertEquals(ERROR, createVtab0Result)

        val errorMessage = sqlite3_errmsg(db)
        val expectedErrorMessage = "Invalid appData, expected 1001 but got 657"
        assertEquals(expectedErrorMessage, errorMessage)

        val createVtab1Sql = "CREATE VIRTUAL TABLE t1 USING kvtab(1001);"
        val createVtab1Result = sqlite3_exec(db, createVtab1Sql, null, null, null)
        assertEquals(OK, createVtab1Result)

        val createVtab2Sql = "CREATE VIRTUAL TABLE t2 USING kvtab(1001);"
        val createVtab2Result = sqlite3_exec(db, createVtab2Sql, null, null, null)
        assertEquals(OK, createVtab2Result)

        assertEquals(0, recorder.destroyCallCount)
        val dropVtab1Result = sqlite3_exec(db, "DROP TABLE t1;", null, null, null)
        assertEquals(OK, dropVtab1Result)
        assertEquals(1, recorder.destroyCallCount)

        // Below sqlite3_drop_modules must not delete the module
        val dropModulesExceptKvtab = sqlite3_drop_modules(db, arrayOf("kvtab"))
        assertEquals(OK, dropModulesExceptKvtab)

        val createVtab3Sql = "CREATE VIRTUAL TABLE t3 USING kvtab(1001);"
        val createVtab3Result = sqlite3_exec(db, createVtab3Sql, null, null, null)
        assertEquals(OK, createVtab3Result)

        val deleteModuleResult = sqlite3_create_module(db, "kvtab", null, null)
        assertEquals(OK, deleteModuleResult)

        // Creating a table will fail and force module cleanup
        assertEquals(0, recorder.disconnectCallCount)
        val createVtab4Sql = "CREATE VIRTUAL TABLE t4 USING kvtab(1001);"
        val createVtab4Result = sqlite3_exec(db, createVtab4Sql, null, null, null)
        assertEquals(ERROR, createVtab4Result)
        assertEquals(2, recorder.disconnectCallCount)
        assertTrue(appDataDestructorCalled)

        val createModule2Result = sqlite3_create_module(db, "kvtab2", module, 0)
        assertEquals(OK, createModule2Result)

        val dropAllModules = sqlite3_drop_modules(db, null)
        assertEquals(OK, dropAllModules)

        val createVtab5Sql = "CREATE VIRTUAL TABLE t5 USING kvtab2(1001);"
        val createVtab5Result = sqlite3_exec(db, createVtab5Sql, null, null, null)
        assertEquals(ERROR, createVtab5Result)

        val dbCloseResult = sqlite3_close(db)
        assertEquals(OK, dbCloseResult)
        assertEquals(3, recorder.createOrConnectCallCount)

        module.close()
    }

    internal fun runSqliteVtabTest(
        afterClose: (KvModuleRecorder.() -> Unit)? = null,
        block: KvModuleRecorder.(db: sqlite3) -> Unit
    ) = runSqliteConnectionTest { db ->
        val recorder = KvModuleRecorder()
        val module = createKvTabModule(recorder)

        val createModuleResult = sqlite3_create_module_v2(db, "kvtab", module, 0, null)
        assertEquals(OK, createModuleResult)

        val createVtabSql = "CREATE VIRTUAL TABLE t USING kvtab(0);"
        val createVTabResult = sqlite3_exec(db, createVtabSql, null, null, null)
        assertEquals(OK, createVTabResult)

        recorder.block(db)

        val dropVtabSql = "DROP TABLE t;"
        val dropVTabResult = sqlite3_exec(db, dropVtabSql, null, null, null)
        assertEquals(OK, dropVTabResult)

        val deleteModuleResult = sqlite3_create_module(db, "kvtab", null, null)
        assertEquals(OK, deleteModuleResult)

        module.close()
        afterClose?.invoke(recorder)
    }

    @Test
    fun constraintPushdownWorks() = runSqliteVtabTest { db ->
        val insertSql = "INSERT INTO t(id, name) VALUES (1, 'Mangue'), (2, 'Melon');"
        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

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
        assertEquals(1, lastConstraintCount)

        // sanity: one filter pass for the point lookup
        assertEquals(1, rowidCallCount.let { filterCallCount })
    }

    @Test
    fun updateAndConflictWorks() = runSqliteVtabTest { db ->
        val insertSql = "INSERT INTO t (id, name) VALUES (1, 'Mangue');"
        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)
        assertEquals(1, updateCallCount)
        assertEquals(ABORT, lastConflictMode)

        // Only `id` is assigned here, so `name` should be read back as "unchanged".
        val nochangeCountBefore = nochangeSeenCount
        val updateSql = "UPDATE t SET id = 1 WHERE id = 1;"
        val updateResult = sqlite3_exec(db, updateSql, null, null, null)
        assertEquals(OK, updateResult)
        assertTrue(nochangeSeenCount > nochangeCountBefore)

        // Conflicting insert resolved with IGNORE should surface via sqlite3_vtab_on_conflict.
        val insertOrIgnoreSql = "INSERT OR IGNORE INTO t (id, name) VALUES (1, 'Duplicate');"
        val insertOrIgnoreResult = sqlite3_exec(db, insertOrIgnoreSql, null, null, null)
        assertEquals(OK, insertOrIgnoreResult)
        assertEquals(IGNORE, lastConflictMode)

        val deleteCountBefore = updateCallCount
        val deleteSql = "DELETE FROM t WHERE id = 1;"
        val deleteResult = sqlite3_exec(db, deleteSql, null, null, null)
        assertEquals(OK, deleteResult)
        assertTrue(updateCallCount > deleteCountBefore)

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
    fun transactionWorks() = runSqliteVtabTest { db ->
        // Even a single autocommit write is wrapped in its own vtab transaction.
        val insertSql = "INSERT INTO t (id, name) VALUES (1, 'Mangue');"
        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        assertEquals(1, beginCallCount)
        assertEquals(2, syncCallCount)
        assertEquals(2, commitCallCount)
        assertEquals(0, rollbackCallCount)

        // An explicit transaction that's rolled back should invoke xRollback instead of xCommit.
        val beginResult = sqlite3_exec(db, "BEGIN;", null, null, null)
        assertEquals(OK, beginResult)

        val insert2Sql = "INSERT INTO t (id, name) VALUES (2, 'Melon');"
        val insert2Result = sqlite3_exec(db, insert2Sql, null, null, null)
        assertEquals(OK, insert2Result)

        val rollbackResult = sqlite3_exec(db, "ROLLBACK;", null, null, null)
        assertEquals(OK, rollbackResult)

        assertEquals(2, beginCallCount)
        assertEquals(2, commitCallCount)
        assertEquals(1, rollbackCallCount)

        var count = -1L
        val selectSql = "SELECT COUNT(*) FROM t;"

        val selectResult = sqlite3_exec(db, selectSql, null, null) { _, _, values, _ ->
            count = assertNotNull(values[0]).toLong()
            0
        }

        assertEquals(OK, selectResult)

        // only the committed row survives
        assertEquals(1L, count)
    }

    @Test
    fun nestedTransactionWorks() = runSqliteVtabTest { db ->
        val beginResult = sqlite3_exec(db, "BEGIN;", null, null, null)
        assertEquals(OK, beginResult)

        val insertSql = "INSERT INTO t (id, name) VALUES (1, 'Mangue');"
        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        val savepointResult = sqlite3_exec(db, "SAVEPOINT sp1;", null, null, null)
        assertEquals(OK, savepointResult)

        val insert2Sql = "INSERT INTO t (id, name) VALUES (2, 'Melon');"
        val insert2Result = sqlite3_exec(db, insert2Sql, null, null, null)
        assertEquals(OK, insert2Result)

        val rollbackResult = sqlite3_exec(db, "ROLLBACK TO sp1;", null, null, null)
        assertEquals(OK, rollbackResult)

        val releaseResult = sqlite3_exec(db, "RELEASE sp1;", null, null, null)
        assertEquals(OK, releaseResult)

        val commitResult = sqlite3_exec(db, "COMMIT;", null, null, null)
        assertEquals(OK, commitResult)

        assertEquals(1, savepointCalls.size)
        assertEquals(1, rollbackToCalls.size)
        assertEquals(1, releaseCalls.size)
        assertEquals(2, commitCallCount)
        assertEquals(0, rollbackCallCount)

        var count = -1L
        val selectSql = "SELECT COUNT(*) FROM t;"

        val selectResult = sqlite3_exec(db, selectSql, null, null) { _, _, values, _ ->
            count = assertNotNull(values[0]).toLong()
            0
        }

        assertEquals(OK, selectResult)

        // row 2 was undone by ROLLBACK TO sp1, row 1 survived the commit
        assertEquals(1L, count)
    }

    @Test
    fun renameWorks() = runSqliteVtabTest { db ->
        assertFalse(renameCalled)

        val renameSql = "ALTER TABLE t RENAME TO t_renamed;"
        val renameResult = sqlite3_exec(db, renameSql, null, null, null)
        assertEquals(OK, renameResult)
        assertTrue(renameCalled)

        val selectSql = "SELECT COUNT(*) FROM t_renamed;"
        val selectResult = sqlite3_exec(db, selectSql, null, null, null)
        assertEquals(OK, selectResult)

        // Just to make outer scope not fails
        val renameBackSql = "ALTER TABLE t_renamed RENAME TO t;"
        val renameBackResult = sqlite3_exec(db, renameBackSql, null, null, null)
        assertEquals(OK, renameBackResult)
    }

    @Test
    fun integrityWorks() = runSqliteVtabTest { db ->
        assertEquals(0, integrityCallCount)

        val integrityResult = sqlite3_exec(db, "PRAGMA integrity_check;", null, null, null)
        assertEquals(OK, integrityResult)
        assertEquals(1, integrityCallCount)
    }

    @Test
    fun findFunctionWorks() = runSqliteVtabTest(
        afterClose = { assertTrue(overloadDestroyCalled) }
    ){ db ->
        val insertSql = "INSERT INTO t (id, name) VALUES (1, 'Mangue'), (2, 'Melon');"
        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        // Global "tag" function: prefixes with "global:". Any non-vtab call should use this one.
        var globalCallCount = 0

        val createGlobalResult = sqlite3_create_function(
            db = db,
            name = "tag",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = Unit,
            func = { _, context, values ->
                globalCallCount++
                val text = assertNotNull(sqlite3_value_text(values[0]))
                sqlite3_result_text(context, "global:$text")
            },
            step = null,
            final = null
        )

        assertEquals(OK, createGlobalResult)

        // Sanity check: without a vtab column argument, the global implementation runs unmodified.
        var actualGlobalValue: String? = null
        val selectGlobalSql = "SELECT tag('x');"

        val selectGlobalResult = sqlite3_exec(db, selectGlobalSql, null, null) { _, _, values, _ ->
            actualGlobalValue = values[0]
            0
        }

        assertEquals(OK, selectGlobalResult)

        val expectedGlobalValue = "global:x"
        assertEquals(expectedGlobalValue, actualGlobalValue)
        assertEquals(1, globalCallCount)
        assertEquals(0, overloadCallCount)

        // Applying "tag" to the vtab's own column should trigger xFindFunction and, since it
        // returns an overload, run the vtab's implementation instead of the global one.
        val actualTaggedValues = mutableListOf<String>()
        val selectTaggedSql = "SELECT tag(name) FROM t ORDER BY id;"

        val selectTaggerResult = sqlite3_exec(db, selectTaggedSql, null, null) { _, _, values, _ ->
            actualTaggedValues.add(assertNotNull(values[0]))
            0
        }

        assertEquals(OK, selectTaggerResult)

        val expectedTaggedValues = listOf("vtab:Mangue", "vtab:Melon")
        assertEquals(expectedTaggedValues, actualTaggedValues)

        assertTrue(findFunctionOfferedCount >= 1)

        // one call per row
        assertEquals(2, overloadCallCount)

        // unchanged: the earlier literal call, not incremented again
        assertEquals(1, globalCallCount)
    }
}