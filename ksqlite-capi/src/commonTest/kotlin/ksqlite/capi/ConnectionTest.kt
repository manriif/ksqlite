package ksqlite.capi

import ksqlite.capi.types.SqliteOutputParam
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the connection logic works.
 */
class ConnectionTest {

    @Test
    fun databaseOpens() = sqliteTest {
        val outDb = SqliteOutputParam()

        val openResult = sqlite3_open_v2(
            fileName = "myDb",
            outDb = outDb,
            flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
            vfs = null
        )

        assertEquals(SqliteResultCode.OK, openResult)

        val db = assertNotNull(outDb.value)
    }
}