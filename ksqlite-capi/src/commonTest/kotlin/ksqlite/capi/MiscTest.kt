package ksqlite.capi

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.types.SqliteCompleteResult
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests miscellaneous SQLite APIs.
 */
class MiscTest {

    @Test
    fun keywordWorks() = runTestNoInit {
        val keywordCount = sqlite3_keyword_count()
        assertTrue(keywordCount > 0)

        val outKeywordName = Utf8OutputParam()
        val keywordResult = sqlite3_keyword_name(0, outKeywordName)

        assertEquals(OK, keywordResult)
        assertNotNull(outKeywordName.value)

        val isKeyword = sqlite3_keyword_check("MATERIALIZED")
        assertEquals(1, isKeyword)
    }

    @Test
    fun comparisonWorks() = runTestNoInit {
        val textA = "textA"
        val textB = "TextBa"
        val textC = "TextBb"

        val stricmpResult = sqlite3_stricmp(textA, textB)
        assertEquals(-1, stricmpResult)

        val strnicmpResult = sqlite3_strnicmp(textB, textC, 5)
        assertEquals(0, strnicmpResult)

        val globResult = sqlite3_strglob("*.txt", "notes.txt")
        assertEquals(0, globResult)

        val likeResult = sqlite3_strlike("Hello%", "Hello World", 0.toChar())
        assertEquals(0, likeResult)
    }

    @Test
    fun otherThingsWorks() = runTestNoInit {
        val sql1 = "CREATE TABLE test"
        val completeResult1 = sqlite3_complete(sql1)
        assertIs<SqliteCompleteResult.Incomplete>(completeResult1)

        val sql2 = "CREATE TABLE test1; CREATE TABLE test2;"
        val completeResult2 = sqlite3_complete(sql2)
        assertIs<SqliteCompleteResult.Complete>(completeResult2)

        val okResultCodeErrStr = sqlite3_errstr(SqliteResultCode.OK)
        assertEquals("not an error", okResultCodeErrStr)
    }

    @Test
    fun memoryWorks() = runSqliteTest {
        val hardHeapLimit = sqlite3_hard_heap_limit64(-1)
        assertEquals(0, hardHeapLimit)

        val softHeapLimit = sqlite3_soft_heap_limit64(-1)
        assertEquals(0, softHeapLimit)

        val memoryUsed = sqlite3_memory_used()
        assertTrue(memoryUsed > 0)

        val memoryHighwater = sqlite3_memory_highwater(0)
        assertTrue(memoryHighwater >= memoryUsed)

        val releaseMemory = sqlite3_release_memory(0)
        assertEquals(0, releaseMemory)
    }

    @Test
    fun statusWorks() = runSqliteTest {
        val outCurrent32 = Int32OutputParam(-1)
        val outHighwater32 = Int32OutputParam(-1)
        val schemaUsedResult = sqlite3_status(MEMORY_USED, outCurrent32, outHighwater32, 0)
        assertEquals(OK, schemaUsedResult)
        assertTrue(outCurrent32.value >= 0)
        assertTrue(outHighwater32.value >= outCurrent32.value)

        val outCurrent64 = Int64OutputParam(-1)
        val outHighwater64 = Int64OutputParam(-1)
        val stmtUsedResult = sqlite3_status64(MALLOC_SIZE, outCurrent64, outHighwater64, 0)
        assertEquals(OK, stmtUsedResult)
        assertEquals(0, outCurrent64.value)
        assertTrue(outHighwater64.value >= outCurrent64.value)
    }
}