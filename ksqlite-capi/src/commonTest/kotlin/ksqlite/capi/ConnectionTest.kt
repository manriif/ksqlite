package ksqlite.capi

import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the connection related functions.
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
    fun errorApisWorks() = runSqliteConnectionTest { db ->
        val outError = Utf8OutputParam()
        val result = sqlite3_exec(db, "CREATE table fail;", outError, null, null)

        assertEquals(SqliteResultCode.ERROR, result)

        val expectedErrorMessage = """near ";": syntax error"""
        assertEquals(expectedErrorMessage, outError.value)

        val errorMessage = sqlite3_errmsg(db)
        assertEquals(expectedErrorMessage, errorMessage)

        val errorOffset = sqlite3_error_offset(db)
        assertEquals(17, errorOffset)

        val errorCode = sqlite3_errcode(db)
        assertEquals(SqliteResultCode.ERROR, errorCode)
    }

    @Test
    fun connectionWorks() = runSqliteConnectionTest { db ->
        val sql = """
            CREATE table fruits(id INTEGER, name TEXT);
            INSERT INTO fruits VALUES (1, 'Pomme'), (2, 'Banane');
            SELECT id, name FROM fruits ORDER BY id;
        """.trimIndent()

        val actualFruits = mutableMapOf<Int, String>()

        val result = sqlite3_exec(db, sql, null, null) { _, count, values, names ->
            assertEquals(2, count)
            assertEquals("id", names[0])
            assertEquals("name", names[1])

            val id = assertNotNull(values[0]).toInt()
            val name = assertNotNull(values[1])

            actualFruits += id to name
            0
        }

        assertEquals(SqliteResultCode.OK, result)

        val expectedFruits = mapOf(1 to "Pomme", 2 to "Banane")
        assertEquals(expectedFruits, actualFruits)

        val lastInsertRowId = sqlite3_last_insert_rowid(db)
        assertEquals(2, lastInsertRowId)

        val changes = sqlite3_changes(db)
        assertEquals(2, changes)

        val changes64 = sqlite3_changes64(db)
        assertEquals(2L, changes64)

        val totalChanges = sqlite3_total_changes(db)
        assertEquals(2, totalChanges)

        val totalChanges64 = sqlite3_total_changes64(db)
        assertEquals(2L, totalChanges64)

        sqlite3_set_last_insert_rowid(db, 64)
        val newLastInsertRowId = sqlite3_last_insert_rowid(db)
        assertEquals(64, newLastInsertRowId)
    }
}