package ksqlite.capi

import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests the connection logic works.
 */
class ConnectionTest {

    @Test
    fun databaseOpens() = runSqliteTest {
        val outDb = SqliteOutputParam()
        val openResult = sqlite3_open(":memory:", outDb)

        assertEquals(SqliteResultCode.OK, openResult)
        assertNotNull(outDb.value)
    }

    @Test
    fun errorApisWorks() = runSqliteConnectionTest { connection ->
        val outError = Utf8OutputParam()
        val result = sqlite3_exec(connection, "CREATE table fail;", outError, null, null)

        assertEquals(SqliteResultCode.ERROR, result)

        val expectedErrorMessage = """near ";": syntax error"""
        assertEquals(expectedErrorMessage, outError.value)

        val errorMessage = sqlite3_errmsg(connection)
        assertEquals(expectedErrorMessage, errorMessage)

        val errorOffset = sqlite3_error_offset(connection)
        assertEquals(17, errorOffset)

        val errorCode = sqlite3_errcode(connection)
        assertEquals(SqliteResultCode.ERROR, errorCode)
    }
}