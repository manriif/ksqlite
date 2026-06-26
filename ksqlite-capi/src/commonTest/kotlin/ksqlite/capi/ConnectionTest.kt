package ksqlite.capi

import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.SqliteSerializeResult
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteRuntimeLimit
import ksqlite.types.SqliteSerializeFlag
import ksqlite.types.SqliteTransactionState
import kotlin.collections.plusAssign
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the connection related functions.
 */
class ConnectionTest {

    @Test
    fun connectionOpens() = runSqliteTest {
        val outDb = SqliteOutputParam()

        val openResult = sqlite3_open(":memory:", outDb)
        assertEquals(SqliteResultCode.OK, openResult)

        val db = assertNotNull(outDb.value)

        val name = sqlite3_db_name(db, 1)
        assertEquals("temp", name)

        val fileName = sqlite3_db_filename(db, "main")
        assertEquals("", fileName)

        val defaultLengthLimit = sqlite3_limit(db, SqliteRuntimeLimit.LENGTH, 100_000)
        assertEquals(1_000_000_000, defaultLengthLimit)

        val newLengthLimit = sqlite3_limit(db, SqliteRuntimeLimit.LENGTH, -1)
        assertEquals(100_000, newLengthLimit)

        val closeResult = sqlite3_close_v2(db)
        assertEquals(SqliteResultCode.OK, closeResult)
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
    fun dataWorks() = runSqliteConnectionTest { db ->
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

        val transactionState = sqlite3_txn_state(db, null)
        assertEquals(SqliteTransactionState.NONE, transactionState)

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

    @Test
    fun columnMetadataWorks() = runSqliteConnectionTest { db ->
        val sql = "CREATE table test(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT);"
        val result = sqlite3_exec(db, sql, null, null, null)
        assertEquals(SqliteResultCode.OK, result)

        val outDataType = Utf8OutputParam()
        val outCollationSequence = Utf8OutputParam()
        val outNotNull = Int32OutputParam()
        val outPrimaryKey = Int32OutputParam()
        val outAutoIncrement = Int32OutputParam()

        val metadataResult = sqlite3_table_column_metadata(
            db = db,
            dbName = null,
            tableName = "test",
            columnName = "id",
            outDataType = outDataType,
            outCollationSequence = outCollationSequence,
            outNotNull = outNotNull,
            outPrimaryKey = outPrimaryKey,
            outAutoIncrement = outAutoIncrement,
        )

        assertEquals(SqliteResultCode.OK, metadataResult)
        assertEquals("INTEGER", outDataType.value)
        assertEquals("BINARY", outCollationSequence.value)
        assertEquals(1, outNotNull.value)
        assertEquals(1, outPrimaryKey.value)
        assertEquals(1, outAutoIncrement.value)
    }

    @Test
    fun serializationWorks() = runSqliteTest {
        // Serialize
        val outSerializeDb = SqliteOutputParam()

        val serializeOpenResult = sqlite3_open(":memory:", outSerializeDb)
        assertEquals(SqliteResultCode.OK, serializeOpenResult)

        val serializeDb = assertNotNull(outSerializeDb.value)

        val insertSql = """
            CREATE table fruits(id INTEGER, name TEXT);
            INSERT INTO fruits VALUES (56, 'Framboise');
        """.trimIndent()

        val insertResult = sqlite3_exec(serializeDb, insertSql, null, null, null)
        assertEquals(SqliteResultCode.OK, insertResult)

        val inMemoryDb = sqlite3_serialize(serializeDb, null, SqliteSerializeFlag.NOCOPY)
        assertIs<SqliteSerializeResult.Failure>(inMemoryDb)

        val copiedDb = sqlite3_serialize(serializeDb, null, null)
        assertIs<SqliteSerializeResult.Mutable>(copiedDb)

        val closeSerializeResult = sqlite3_close_v2(serializeDb)
        assertEquals(SqliteResultCode.OK, closeSerializeResult)

        // Deserialize
        val outDeserializeDb = SqliteOutputParam()

        val deserializeOpenResult = sqlite3_open(":memory:", outDeserializeDb)
        assertEquals(SqliteResultCode.OK, deserializeOpenResult)

        val deserializeDb = assertNotNull(outDeserializeDb.value)
        val dbContent = copiedDb.buffer

        val deserializeResult = sqlite3_deserialize(
            db = deserializeDb,
            database = null,
            buffer = dbContent,
            dbSize = copiedDb.databaseSize,
            bufferSize = dbContent.byteSize,
            flags = SqliteDeserializeFlag.READONLY or SqliteDeserializeFlag.FREEONCLOSE
        )

        assertEquals(SqliteResultCode.OK, deserializeResult)

        val readSql = "SELECT * from fruits;"
        var callbackCalled = false

        val readResult = sqlite3_exec(deserializeDb, readSql, null, null) { _, count, values, names ->
            assertEquals(2, count)
            assertEquals("id", names[0])
            assertEquals("name", names[1])

            val id = assertNotNull(values[0]).toInt()
            val name = assertNotNull(values[1])

            assertEquals(56, id)
            assertEquals("Framboise", name)
            callbackCalled = true
            0
        }

        assertEquals(SqliteResultCode.OK, readResult)
        assertTrue(callbackCalled)

        val writeSql = "INSERT INTO fruits VALUES (10, 'Mangue');"

        val writeResult = sqlite3_exec(deserializeDb, writeSql, null, null, null)
        assertEquals(SqliteResultCode.READONLY, writeResult)

        val closeDeserializeResult = sqlite3_close_v2(deserializeDb)
        assertEquals(SqliteResultCode.OK, closeDeserializeResult)
    }
}