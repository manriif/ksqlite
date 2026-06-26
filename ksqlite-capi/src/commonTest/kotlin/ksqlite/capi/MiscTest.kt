package ksqlite.capi

import ksqlite.capi.types.Utf8OutputParam
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MiscTest {

    @Test
    fun keywordApisWorks() {
        val keywordCount = sqlite3_keyword_count()
        assertTrue(keywordCount > 0)

        val outKeywordName = Utf8OutputParam()
        val keywordResult = sqlite3_keyword_name(0, outKeywordName)

        assertEquals(SqliteResultCode.OK, keywordResult)
        assertNotNull(outKeywordName.value)

        val isKeyword = sqlite3_keyword_check("MATERIALIZED")
        assertEquals(1, isKeyword)
    }
}