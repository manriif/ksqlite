package ksqlite.capi

import ksqlite.capi.callbacks.SqliteAutoExtensionCallback
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.types.SqliteTraceEventCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests the different callbacks.
 */
class CallbackTest {

    @Test
    fun autoExtensionWorks() = runSqliteTest {
        // Called
        val extension2ErrorMessage = "AutoExtension 2 failed"
        var autoExtension1Db: sqlite3? = null
        var autoExtension2Db: sqlite3? = null

        val autoExtension1 = SqliteAutoExtensionCallback { db ->
            autoExtension1Db = db
            success()
        }

        val autoExtension1Result = sqlite3_auto_extension(autoExtension1)
        assertEquals(OK, autoExtension1Result)

        val autoExtension2 = SqliteAutoExtensionCallback { db ->
            autoExtension2Db = db
            failure(CANTOPEN, extension2ErrorMessage)
        }

        val autoExtension2Result = sqlite3_auto_extension(autoExtension2)
        assertEquals(OK, autoExtension2Result)

        val autoExtension3Result = sqlite3_auto_extension {
            fail("AutoExtension 3 should no have been called after the failure of AutoExtension 2")
        }

        assertEquals(OK, autoExtension3Result)

        val outDb = sqlite3.OutputParam()
        val openResult = sqlite3_open(":memory:", outDb)
        assertEquals(CANTOPEN, openResult)

        val db = assertNotNull(outDb.value)
        assertEquals(db, autoExtension1Db)
        assertEquals(db, autoExtension2Db)

        val dbErrorMessage = sqlite3_errmsg(outDb.value!!)
        assertNotNull(dbErrorMessage)
        assertTrue(dbErrorMessage.endsWith(extension2ErrorMessage))

        val execResult = sqlite3_exec(outDb.value!!, "CREATE TABLE x(a INTEGER);", null, null, null)
        assertEquals(MISUSE, execResult)

        val closeResult = sqlite3_close(db)
        assertEquals(OK, closeResult)

        // Cancelled
        autoExtension1Db = null
        autoExtension2Db = null

        val cancelAutoExtension1Result = sqlite3_cancel_auto_extension(autoExtension1)
        assertEquals(1, cancelAutoExtension1Result)

        val outDb2 = sqlite3.OutputParam()
        val open2Result = sqlite3_open(":memory:", outDb2)
        assertEquals(CANTOPEN, open2Result)

        val db2 = assertNotNull(outDb2.value)
        assertNull(autoExtension1Db)
        assertEquals(db2, autoExtension2Db)

        val closeDb2Result = sqlite3_close(db2)
        assertEquals(OK, closeDb2Result)

        // Reset
        sqlite3_reset_auto_extension()
        autoExtension2Db = null

        val outDb3 = sqlite3.OutputParam()
        val open3Result = sqlite3_open(":memory:", outDb3)
        assertEquals(OK, open3Result)

        val db3 = assertNotNull(outDb3.value)
        assertNull(autoExtension1Db)
        assertNull(autoExtension2Db)

        val closeDb3Result = sqlite3_close(db3)
        assertEquals(OK, closeDb3Result)
    }

    @Test
    fun autovacuumPagesWorks() = runSqliteTest {
        val outDb = sqlite3.OutputParam()
        val openResult = sqlite3_open("", outDb)
        assertEquals(OK, openResult)

        val db = assertNotNull(outDb.value)
        val pageSize = 4096
        val pragmaSql = "PRAGMA auto_vacuum = FULL; PRAGMA page_size = $pageSize;"
        val pragmaResult = sqlite3_exec(db, pragmaSql, null, null, null)
        assertEquals(OK, pragmaResult)

        var destroyerCalled = false
        var callbackCallCount = 0

        val autovacuumPagesResult = sqlite3_autovacuum_pages(
            db = db,
            appData = 90,
            destroy = { appData ->
                assertEquals(90, appData)
                destroyerCalled = true
            },
            callback = { appData, schemaName, _, _, bytePerPage ->
                assertEquals(90, appData)
                assertEquals("main", schemaName)
                callbackCallCount += 1
                bytePerPage
            }
        )

        assertEquals(OK, autovacuumPagesResult)

        val createTableResult = sqlite3_exec(db, "CREATE TABLE t (data BLOB);", null, null, null)
        assertEquals(OK, createTableResult)

        val hexBlob = "AB".repeat(pageSize / 2)

        repeat(6) {
            val result = sqlite3_exec(db, "INSERT INTO t VALUES (X'$hexBlob');", null, null, null)
            assertEquals(OK, result)
        }

        val pageCountSql = "PRAGMA page_count;"
        var pageCount = 0

        val pageCountResult = sqlite3_exec(db, pageCountSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            pageCount = assertNotNull(values[0]).toInt()
            0
        }

        assertEquals(OK, pageCountResult)
        assertTrue(pageCount >= 3)

        val freeListSql = "PRAGMA freeList_count;"
        var freeList = 0

        val freeListResult = sqlite3_exec(db, freeListSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            freeList = assertNotNull(values[0]).toInt()
            0
        }

        assertEquals(OK, freeListResult)
        assertEquals(0, freeList)

        val deleteSql = "BEGIN; DELETE FROM t; COMMIT;"
        val deleteResult = sqlite3_exec(db, deleteSql, null, null, null)
        assertEquals(OK, deleteResult)
        assertTrue(callbackCallCount > 0)

        val closeResult = sqlite3_close(db)
        assertEquals(OK, closeResult)
        assertTrue(destroyerCalled)
    }

    @Test
    fun busyHandlerWorks() = runSqliteTest { isWasm ->
        val outDb = sqlite3.OutputParam()
        val openResult = sqlite3_open(":memory:", outDb)
        assertEquals(OK, openResult)

        val db = assertNotNull(outDb.value)
        val busyTimeoutResult = sqlite3_busy_timeout(db, 100.milliseconds)
        assertEquals(OK, busyTimeoutResult)

        val closeResult = sqlite3_close(db)
        assertEquals(OK, closeResult)

        // TODO use OPFS VFS on WASM as default VFS may not support locking.
        val vfsName: String? = if (isWasm) return@runSqliteTest else null
        val vfs = assertNotNull(sqlite3_vfs_find(vfsName))

        vfs.usingRealTempFile("busy.db") { path ->
            val outDb1 = sqlite3.OutputParam()
            val openDb1Result = sqlite3_open(path, outDb1)
            assertEquals(OK, openDb1Result)
            val db1 = assertNotNull(outDb1.value)

            val outDb2 = sqlite3.OutputParam()
            val openDb2Result = sqlite3_open(path, outDb2)
            assertEquals(OK, openDb2Result)

            val db2 = assertNotNull(outDb2.value)
            var busyCalled = false

            val busyHandlerResult = sqlite3_busy_handler(db2, 453) { appData, _ ->
                assertEquals(453, appData)
                busyCalled = true
                0
            }

            assertEquals(OK, busyHandlerResult)

            val lockSql = "BEGIN EXCLUSIVE; CREATE TABLE test(text TEXT);"
            val lockResult = sqlite3_exec(db1, lockSql, null, null, null)
            assertEquals(OK, lockResult)
            assertFalse(busyCalled)

            val accessSql = "SELECT * FROM test;"
            val accessResult = sqlite3_exec(db2, accessSql, null, null, null)
            assertEquals(BUSY, accessResult)
            assertTrue(busyCalled)

            val closeDb1Result = sqlite3_close(db1)
            assertEquals(OK, closeDb1Result)

            val closeDb2Result = sqlite3_close(db2)
            assertEquals(OK, closeDb2Result)
        }
    }

    @Test
    fun collationWorks() = runSqliteTest {
        val outDb = sqlite3.OutputParam()
        val openResult = sqlite3_open(":memory:", outDb)
        assertEquals(OK, openResult)

        val db = assertNotNull(outDb.value)
        val collationName = "testComparator"
        var destructorCalled = false
        var callbackCalledCount = 0

        val collationNeededResult = sqlite3_collation_needed(db, 6) { appData, db, eTextRep, name ->
            assertEquals(6, appData)
            assertEquals(collationName, name)
            assertEquals(UTF8, eTextRep)

            val createCollationResult = sqlite3_create_collation_v2(
                db = db,
                name = collationName,
                encoding = UTF8,
                appData = 98005,
                destroy = { appData ->
                    assertEquals(98005, appData)
                    destructorCalled = true
                },
                callback = { appData, lhs, rhs ->
                    assertEquals(98005, appData)
                    callbackCalledCount++
                    lhs.decodeToString().compareTo(rhs.decodeToString())
                }
            )

            assertEquals(OK, createCollationResult)
        }

        assertEquals(OK, collationNeededResult)

        val sql = """
            CREATE TABLE fruit(name TEXT NOT NULL COLLATE $collationName);
            INSERT INTO fruit VALUES ('Ananas'), ('Datte'), ('Fraise'), ('Citron'), ('Banane');
            SELECT * from fruit ORDER BY name DESC;
        """.trimIndent()

        val actualValues = mutableListOf<String>()

        val execResult = sqlite3_exec(db, sql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            actualValues.add(assertNotNull(values[0]))
            0
        }

        assertEquals(OK, execResult)
        assertEquals(8, callbackCalledCount)

        val expectedValues = listOf("Fraise", "Datte", "Citron", "Banane", "Ananas")
        assertEquals(expectedValues, actualValues)

        val closeResult = sqlite3_close(db)
        assertEquals(OK, closeResult)
        assertTrue(destructorCalled)
    }

    @Test
    fun commitAndRollbacksHooksWorks() = runSqliteConnectionDataTest { db ->
        var commitCalled = false
        var rollbackCalled = false

        sqlite3_commit_hook(db, 96) { appData ->
            assertEquals(96, appData)
            commitCalled = true
            1
        }

        sqlite3_rollback_hook(db, 7369) { appData ->
            assertEquals(7369, appData)
            rollbackCalled = true
        }

        val sql = "INSERT INTO test VALUES (45, 18.60, 'Prune', x'0001', zeroblob(65));"
        val execResult = sqlite3_exec(db, sql, null, null, null)
        assertEquals(CONSTRAINT, execResult)

        assertTrue(commitCalled)
        assertTrue(rollbackCalled)
    }

    @Test
    fun preupdateHookWorks() = runSqliteConnectionTest { db ->
        val insertSql = """
            CREATE TABLE fruit(name TEXT NOT NULL);
            INSERT INTO fruit VALUES ('Ananas');
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        var callbackCalled = false

        sqlite3_preupdate_hook(db, 185) { data, idb, dml, dbName, tableName, oldRowid, newRowid ->
            assertEquals(185, data)
            assertEquals(db, idb)
            assertEquals(UPDATE, dml)
            assertEquals("main", dbName)
            assertEquals("fruit", tableName)
            assertEquals(1, oldRowid)
            assertEquals(1, newRowid)

            val count = sqlite3_preupdate_count(db)
            assertEquals(1, count)

            val depth = sqlite3_preupdate_depth(db)
            assertEquals(0, depth)

            val blobColumn = sqlite3_preupdate_blobwrite(db)
            assertEquals(-1, blobColumn)

            val outOld = sqlite3_value.OutputParam()
            val oldResult = sqlite3_preupdate_old(db, 0, outOld)
            assertEquals(OK, oldResult)

            val oldValue = assertNotNull(outOld.value)
            val oldName = sqlite3_value_text(oldValue)
            assertEquals("Ananas", oldName)

            val outNew = sqlite3_value.OutputParam()
            val newResult = sqlite3_preupdate_new(db, 0, outNew)
            assertEquals(OK, newResult)

            val newValue = assertNotNull(outNew.value)
            val newName = sqlite3_value_text(newValue)
            assertEquals("Framboise", newName)

            callbackCalled = true
        }

        val updateSql = "UPDATE fruit SET name = 'Framboise' WHERE name = 'Ananas';"
        val updateResult = sqlite3_exec(db, updateSql, null, null, null)
        assertEquals(OK, updateResult)
        assertTrue(callbackCalled)
    }

    @Test
    fun progressHandlerWorks() = runSqliteConnectionDataTest { db ->
        var callbackCallCount = 0

        sqlite3_progress_handler(db, 1, 50) { appData ->
            assertEquals(50, appData)
            callbackCallCount++
            0
        }

        val sql = "INSERT INTO test VALUES (45, 18.60, 'Prune', x'0001', zeroblob(65));"
        val execResult = sqlite3_exec(db, sql, null, null, null)
        assertEquals(OK, execResult)
        assertTrue(callbackCallCount > 0)
    }

    @Test
    fun authorizerWorks() = runSqliteConnectionTest { db ->
        var createTableCalled = false
        var insertCalled = false
        var createTriggerCalled = false
        var dropTableCalled = false

        val setAuthorizerResult = sqlite3_set_authorizer(db, 1) { appData, action, d1, d2, _, _ ->
            assertEquals(1, appData)

            when (action) {
                CREATE_TABLE -> {
                    assertEquals("fruit", d1)
                    createTableCalled = true
                    OK
                }

                INSERT -> if (d1 == "fruit") {
                    insertCalled = true
                    DENY
                } else {
                    OK
                }

                CREATE_TRIGGER -> {
                    assertEquals("select_fruits", d1)
                    assertEquals("fruit", d2)
                    createTriggerCalled = true
                    IGNORE
                }

                DROP_TABLE -> {
                    assertEquals("fruit", d1)
                    dropTableCalled = true
                    DENY
                }

                UPDATE, READ, DELETE -> OK
                else -> DENY
            }
        }

        assertEquals(OK, setAuthorizerResult)

        val insertSql = """
            CREATE TABLE fruit(name TEXT NOT NULL);
            INSERT INTO fruit VALUES ('Kiwi');
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(AUTH, insertResult)
        assertTrue(createTableCalled)
        assertTrue(insertCalled)

        val createTriggerSql = """
            CREATE TRIGGER select_fruits
            AFTER INSERT ON fruit
            BEGIN
                SELECT * FROM fruit;
            END;
        """.trimIndent()

        val createTriggerResult = sqlite3_exec(db, createTriggerSql, null, null, null)
        assertEquals(OK, createTriggerResult)
        assertTrue(createTriggerCalled)

        val dropTableSql = "DROP TABLE fruit;"
        val dropTableResult = sqlite3_exec(db, dropTableSql, null, null, null)
        assertEquals(AUTH, dropTableResult)
        assertTrue(dropTableCalled)
    }

    @Test
    fun traceWorks() = runSqliteTest {
        val outDb = sqlite3.OutputParam()
        val openResult = sqlite3_open(":memory:", outDb)
        assertEquals(OK, openResult)

        val db = assertNotNull(outDb.value)

        val insertSql = """
            CREATE TABLE fruit(name TEXT NOT NULL);
            INSERT INTO fruit VALUES ('Kiwi');
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        val selectSql = "SELECT * FROM fruit;"
        val traceFlags = SqliteTraceEventCode.STMT or PROFILE or ROW or CLOSE
        var stmtCalled = false
        var profileCalled = false
        var rowCalled = false
        var closeCalled = false

        val traceResult = sqlite3_trace_v2(db, traceFlags, 123) { appData, event ->
            assertEquals(123, appData)

            when (event) {
                is Stmt -> {
                    assertEquals(selectSql, event.sql)
                    stmtCalled = true
                }

                is Profile -> {
                    assertTrue(event.nanos >= 0)
                    profileCalled = true
                }

                is Row -> rowCalled = true

                is Close -> {
                    assertEquals(db, event.db)
                    closeCalled = true
                }
            }

            0
        }

        assertEquals(OK, traceResult)

        val selectResult = sqlite3_exec(db, selectSql, null, null, null)
        assertEquals(OK, selectResult)
        assertTrue(stmtCalled)
        assertTrue(profileCalled)
        assertTrue(rowCalled)

        val closeResult = sqlite3_close(db)
        assertEquals(OK, closeResult)
        assertTrue(closeCalled)
    }

    @Test
    fun updateHookWorks() = runSqliteConnectionTest { db ->
        val insertSql = """
            CREATE TABLE fruit(name TEXT NOT NULL);
            INSERT INTO fruit VALUES ('Ananas');
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        var callbackCalled = false

        sqlite3_update_hook(db, 79787) { appData, dml, dbName, tableName, rowid ->
            assertEquals(79787, appData)
            assertEquals(UPDATE, dml)
            assertEquals("main", dbName)
            assertEquals("fruit", tableName)
            assertEquals(1, rowid)

            callbackCalled = true
        }

        val updateSql = "UPDATE fruit SET name = 'Framboise' WHERE name = 'Ananas';"
        val updateResult = sqlite3_exec(db, updateSql, null, null, null)
        assertEquals(OK, updateResult)
        assertTrue(callbackCalled)
    }

    @Test
    fun walHookWorks() = runSqliteTest { isWasm ->
        // TODO use a VFS that supports WAL on WASM
        val vfsName: String? = if (isWasm) return@runSqliteTest else null
        val vfs = assertNotNull(sqlite3_vfs_find(vfsName))

        vfs.usingRealTempFile("wal.db") { path ->
            val outDb = sqlite3.OutputParam()
            val openResult = sqlite3_open(path, outDb)
            assertEquals(OK, openResult)

            val db = assertNotNull(outDb.value)
            val walPragma = "PRAGMA journal_mode=WAL;"
            val walPragmaResult = sqlite3_exec(db, walPragma, null, null, null)
            assertEquals(OK, walPragmaResult)

            var callbackCallCount = 0

            sqlite3_wal_hook(db, 1789) { appData, idb, databaseName, pageCount ->
                assertEquals(1789, appData)
                assertEquals(db, idb)
                assertEquals("main", databaseName)
                assertTrue(pageCount >= 0)
                callbackCallCount++
                OK
            }

            val sql = """
                CREATE TABLE test(x INTEGER NOT NULL);
                BEGIN;
                INSERT INTO test VALUES (1);
                COMMIT;
            """.trimIndent()

            val execResult = sqlite3_exec(db, sql, null, null, null)
            assertEquals(OK, execResult)
            assertTrue(callbackCallCount > 0)

            val sql2 = """
                BEGIN;
                INSERT INTO test VALUES (2);
                INSERT INTO test VALUES (3);
                INSERT INTO test VALUES (4);
                COMMIT;
            """.trimIndent()

            val beforeExec2CallbackCallCount = callbackCallCount
            val exec2Result = sqlite3_exec(db, sql2, null, null, null)
            assertEquals(OK, exec2Result)

            val outNLog = Int32OutputParam(-1)
            val outNCkpt = Int32OutputParam(-1)
            val checkpointV2Result = sqlite3_wal_checkpoint_v2(db, null, PASSIVE, outNLog, outNCkpt)
            assertEquals(OK, checkpointV2Result)
            assertTrue(callbackCallCount > beforeExec2CallbackCallCount)
            assertTrue(outNLog.value >= 0)
            assertTrue(outNCkpt.value >= 0)

            val afterExec2CallbackCallCount = callbackCallCount
            val autoCheckpointResult = sqlite3_wal_autocheckpoint(db, 1000)
            assertEquals(OK, autoCheckpointResult)

            val checkpointResult = sqlite3_wal_checkpoint(db, null)
            assertEquals(OK, checkpointResult)
            assertEquals(afterExec2CallbackCallCount, callbackCallCount)

            val closeResult = sqlite3_close(db)
            assertEquals(OK, closeResult)
        }
    }
}