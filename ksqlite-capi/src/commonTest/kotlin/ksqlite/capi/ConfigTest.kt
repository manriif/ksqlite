package ksqlite.capi

import ksqlite.capi.memory.OpaqueBuffer
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteSqlLogEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests SQLite configuration.
 */
class ConfigTest {

    /**
     * /!\ These tests may affect other tests if they share the same SQLite instance. Should be
     * executed isolated if possible (or default values must be restored then).
     */
    @Test
    fun configsWorks() = runTestNoInit {
        val singleThreadResult = sqlite3_config(SqliteConfigOption.SINGLETHREAD)
        assertEquals(SqliteResultCode.OK, singleThreadResult)

        val multiThreadResult = sqlite3_config(SqliteConfigOption.MULTITHREAD)
        assertEquals(SqliteResultCode.OK, multiThreadResult)

        val serializedResult = sqlite3_config(SqliteConfigOption.SERIALIZED)
        assertEquals(SqliteResultCode.OK, serializedResult)

        val memStatus = SqliteConfigOption.MEMSTATUS(1)
        val memStatusResult = sqlite3_config(memStatus)
        assertEquals(SqliteResultCode.OK, memStatusResult)

        val lookaside = SqliteConfigOption.LOOKASIDE(128, 2)
        val lookasideResult = sqlite3_config(lookaside)
        assertEquals(SqliteResultCode.OK, lookasideResult)

        val uri = SqliteConfigOption.URI(1)
        val uriResult = sqlite3_config(uri)
        assertEquals(SqliteResultCode.OK, uriResult)

        val coveringIndexScan = SqliteConfigOption.COVERING_INDEX_SCAN(1)
        val coveringIndexScanResult = sqlite3_config(coveringIndexScan)
        assertEquals(SqliteResultCode.OK, coveringIndexScanResult)

        val mmapSize = SqliteConfigOption.MMAP_SIZE(128, 2)
        val mmapSizeResult = sqlite3_config(mmapSize)
        assertEquals(SqliteResultCode.OK, mmapSizeResult)

        val outPCacheHdrsz = Int32OutputParam(-1)
        val pCacheHdrsz = SqliteConfigOption.PCACHE_HDRSZ(outPCacheHdrsz)
        val pCacheHdrszResult = sqlite3_config(pCacheHdrsz)
        assertEquals(SqliteResultCode.OK, pCacheHdrszResult)
        assertNotEquals(-1, outPCacheHdrsz.value)

        val pmaSz = SqliteConfigOption.PMASZ(1U)
        val pmaSzResult = sqlite3_config(pmaSz)
        assertEquals(SqliteResultCode.OK, pmaSzResult)

        val stmtJrnlSpill = SqliteConfigOption.STMTJRNL_SPILL(1)
        val stmtJrnlSpillResult = sqlite3_config(stmtJrnlSpill)
        assertEquals(SqliteResultCode.OK, stmtJrnlSpillResult)

        val smallMalloc = SqliteConfigOption.SMALL_MALLOC(1)
        val smallMallocResult = sqlite3_config(smallMalloc)
        assertEquals(SqliteResultCode.OK, smallMallocResult)

        val memdbMaxsize = SqliteConfigOption.MEMDB_MAXSIZE(128)
        val memdbMaxsizeResult = sqlite3_config(memdbMaxsize)
        assertEquals(SqliteResultCode.OK, memdbMaxsizeResult)

        val outRowidInView = Int32OutputParam(-1)
        val rowidInView = SqliteConfigOption.ROWID_IN_VIEW(outRowidInView)
        val rowidInViewResult = sqlite3_config(rowidInView)
        assertEquals(SqliteResultCode.OK, rowidInViewResult)
        assertNotEquals(-1, outRowidInView.value)

        val pageCacheBuffer = assertNotNull(OpaqueBuffer.allocate(128))
        val pageCache = SqliteConfigOption.PAGECACHE(pageCacheBuffer, 128, 2)
        val pageCacheResult = sqlite3_config(pageCache)
        assertEquals(SqliteResultCode.OK, pageCacheResult)
        pageCacheBuffer.close()
    }

    @Test
    fun configLogWorks() = runTestNoInit {
        val logCode = 8458
        val logMessage = "Corrupted file system"
        var logCalled = false

        val log = SqliteConfigOption.LOG(null) { _, errorCode, errorMessage ->
            assertEquals(logCode, errorCode)
            assertEquals(logMessage, errorMessage)
            logCalled = true
        }

        val logResult = sqlite3_config(log)
        assertEquals(SqliteResultCode.OK, logResult)

        sqlite3_log(logCode, logMessage)
        assertTrue(logCalled)
    }

    @Test
    fun configSqlLogWorks() = runTestNoInit {
        val fileName = "sqllogtest"
        val statement = "CREATE TABLE sqllog(name TEXT NOT NULL);"
        var databaseOpened = false
        var databaseClosed = false
        var statementExecuted = false

        val sqlLog = SqliteConfigOption.SQLLOG(null) { _, _, event ->
            when (event) {
                SqliteSqlLogEvent.DatabaseClosed -> databaseClosed = true

                is SqliteSqlLogEvent.DatabaseOpened -> {
                    assertEquals(fileName, event.dbFileName)
                    databaseOpened = true
                }

                is SqliteSqlLogEvent.StatementExecuted -> {
                    assertEquals(statement, event.statement)
                    statementExecuted = true
                }
            }
        }

        val sqlLogResult = sqlite3_config(sqlLog)
        assertEquals(SqliteResultCode.OK, sqlLogResult)

        val initResult = sqlite3_initialize()
        assertEquals(SqliteResultCode.OK, initResult)

        val outDb = SqliteOutputParam()

        val openResult = sqlite3_open_v2(
            fileName = fileName,
            outDb = outDb,
            flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
            vfs = null
        )

        assertEquals(SqliteResultCode.OK, openResult)

        val db = assertNotNull(outDb.value)
        assertTrue(databaseOpened)

        val execResult = sqlite3_exec(db, statement, null, null, null)
        assertEquals(SqliteResultCode.OK, execResult)
        assertTrue(statementExecuted)

        val closeResult = sqlite3_close(db)
        assertEquals(SqliteResultCode.OK, closeResult)
        assertTrue(databaseClosed)

        val shutdownResult = sqlite3_shutdown()
        assertEquals(SqliteResultCode.OK, shutdownResult)
    }
}