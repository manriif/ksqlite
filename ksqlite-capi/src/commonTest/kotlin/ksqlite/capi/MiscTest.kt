package ksqlite.capi

import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.types.SqliteCompleteResult
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
    fun keywordApisWorks() = runTestNoInit {
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
    }
}