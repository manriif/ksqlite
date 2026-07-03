package ksqlite.capi

import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.types.SqliteFileControlOpcode
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests the different callbacks.
 */
class CallbackTest {

    @Test
    fun autoExtensionWorks() = runSqliteTest {
        val extension2ErrorMessage = "AutoExtension 2 failed"
        var callback1Db: sqlite3? = null
        var callback2Db: sqlite3? = null

        val autoExtension1Result = sqlite3_auto_extension { db ->
            callback1Db = db
            success()
        }

        assertEquals(SqliteResultCode.OK, autoExtension1Result)

        val autoExtension2Result = sqlite3_auto_extension { db ->
            callback2Db = db
            failure(SqliteResultCode.CANTOPEN, extension2ErrorMessage)
        }

        assertEquals(SqliteResultCode.OK, autoExtension2Result)

        val autoExtension3Result = sqlite3_auto_extension {
            fail("AutoExtension 3 should no have been called after the failure of AutoExtension 2")
        }

        assertEquals(SqliteResultCode.OK, autoExtension3Result)

        val outDb = sqlite3.OutputParam()
        val openResult = sqlite3_open(":memory:", outDb)
        assertEquals(SqliteResultCode.CANTOPEN, openResult)

        val db = assertNotNull(outDb.value)
        assertEquals(db, callback1Db)
        assertEquals(db, callback2Db)

        val dbErrorMessage = sqlite3_errmsg(outDb.value!!)
        assertNotNull(dbErrorMessage)
        assertTrue(dbErrorMessage.endsWith(extension2ErrorMessage))

        val execResult = sqlite3_exec(outDb.value!!, "CREATE TABLE x(a INTEGER);", null, null, null)
        assertEquals(SqliteResultCode.MISUSE, execResult)

        val closeResult = sqlite3_close(db)
        assertEquals(SqliteResultCode.OK, closeResult)
    }

    @Test
    fun autovacuumPagesWorks() = runSqliteTest {
        val outDb = sqlite3.OutputParam()
        val openResult = sqlite3_open("", outDb)
        assertEquals(SqliteResultCode.OK, openResult)

        val db = assertNotNull(outDb.value)
        val pageSize = 4096
        val pragmaSql = "PRAGMA auto_vacuum = FULL; PRAGMA page_size = $pageSize;"
        val pragmaResult = sqlite3_exec(db, pragmaSql, null, null, null)
        assertEquals(SqliteResultCode.OK, pragmaResult)

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

        assertEquals(SqliteResultCode.OK, autovacuumPagesResult)

        val createTableResult = sqlite3_exec(db, "CREATE TABLE t (data BLOB);", null, null, null)
        assertEquals(SqliteResultCode.OK, createTableResult)

        val hexBlob = "AB".repeat(pageSize / 2)

        repeat(6) {
            val result = sqlite3_exec(db, "INSERT INTO t VALUES (X'$hexBlob');", null, null, null)
            assertEquals(SqliteResultCode.OK, result)
        }

        val pageCountSql = "PRAGMA page_count;"
        var pageCount = 0

        val pageCountResult = sqlite3_exec(db, pageCountSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            pageCount = assertNotNull(values[0]).toInt()
            0
        }

        assertEquals(SqliteResultCode.OK, pageCountResult)
        assertTrue(pageCount >= 3)

        val freeListSql = "PRAGMA freeList_count;"
        var freeList = 0

        val freeListResult = sqlite3_exec(db, freeListSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            freeList = assertNotNull(values[0]).toInt()
            0
        }

        assertEquals(SqliteResultCode.OK, freeListResult)
        assertEquals(0, freeList)

        val deleteResult = sqlite3_exec(db, "BEGIN; DELETE FROM t; COMMIT;", null, null, null)
        assertEquals(SqliteResultCode.OK, deleteResult)

        println("AutovacuumPages was called $callbackCallCount times")
        assertTrue(callbackCallCount > 0)

        val closeResult = sqlite3_close(db)
        assertEquals(SqliteResultCode.OK, closeResult)

        assertTrue(destroyerCalled)
    }

    @Test
    fun busyHandlerWorks() = runSqliteTest { isWasm ->
        // TODO use OPFS VFS on WASM as default VFS may not support locking.
        val vfsName: String? = if (isWasm) return@runSqliteTest else null
        val vfs = assertNotNull(sqlite3_vfs_find(vfsName))

        vfs.usingRealTempFile("busy.db") { path ->
            val outDb1 = sqlite3.OutputParam()
            val openDb1Result = sqlite3_open(path, outDb1)
            assertEquals(SqliteResultCode.OK, openDb1Result)
            val db1 = assertNotNull(outDb1.value)

            val outDb2 = sqlite3.OutputParam()
            val openDb2Result = sqlite3_open(path, outDb2)
            assertEquals(SqliteResultCode.OK, openDb2Result)
            val db2 = assertNotNull(outDb2.value)

            var busyCalled = false

            val busyHandlerResult = sqlite3_busy_handler(db2, 453) { appData, _ ->
                assertEquals(453, appData)
                busyCalled = true
                0
            }

            val outName = Utf8OutputParam()
            println(sqlite3_file_control(db1, "main", SqliteFileControlOpcode.VFSNAME(outName)))
            println(outName.value)

            assertEquals(SqliteResultCode.OK, busyHandlerResult)

            val lockSql = "BEGIN EXCLUSIVE; CREATE TABLE test(text TEXT);"
            val lockResult = sqlite3_exec(db1, lockSql, null, null, null)
            assertEquals(SqliteResultCode.OK, lockResult)

            assertFalse(busyCalled)

            val accessSql = "SELECT * FROM test;"
            val accessResult = sqlite3_exec(db2, accessSql, null, null, null)
            println(sqlite3_errmsg(db2))
            println(sqlite3_extended_errcode(db2))
            assertEquals(SqliteResultCode.BUSY, accessResult)

            assertTrue(busyCalled)

            val closeDb1Result = sqlite3_close(db1)
            assertEquals(SqliteResultCode.OK, closeDb1Result)

            val closeDb2Result = sqlite3_close(db2)
            assertEquals(SqliteResultCode.OK, closeDb2Result)
        }
    }
}