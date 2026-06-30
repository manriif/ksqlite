package ksqlite.capi

import ksqlite.capi.memory.OpaqueBuffer
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.SqliteSerializeResult
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteRuntimeLimit
import ksqlite.types.SqliteSerializeFlag
import ksqlite.types.SqliteTransactionState
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
            CREATE TABLE fruits(id INTEGER, name TEXT);
            INSERT INTO fruits VALUES (1, 'Pomme'), (2, 'Banane');
            SELECT id, name FROM fruits ORDER BY id;
        """.trimIndent()

        val actualFruits = mutableMapOf<Int, String>()

        val result = sqlite3_exec(db, sql, null, 56) { appData, count, values, names ->
            assertEquals(56, appData)
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
        val sql = "CREATE TABLE test(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT);"
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
            CREATE TABLE fruits(id INTEGER, name TEXT);
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

        val readSql = "SELECT * FROM fruits;"
        var callbackCalled = false

        val readResult =
            sqlite3_exec(deserializeDb, readSql, null, null) { _, count, values, names ->
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

    @Test
    fun configWorks() = runSqliteConnectionTest { db ->
        val actualName = sqlite3_db_name(db, 0)
        assertEquals("main", actualName)

        val newName = "theMainDatabase"
        val mainDbName = SqliteDbConfigOption.MAINDBNAME(newName)
        val mainDbNameResult = sqlite3_db_config(db, mainDbName)
        assertEquals(SqliteResultCode.OK, mainDbNameResult)

        val updatedName = sqlite3_db_name(db, 0)
        assertEquals(newName, updatedName)

        val outEnableFkey = Int32OutputParam(-1)
        val enableFkey = SqliteDbConfigOption.ENABLE_FKEY(1, outEnableFkey)
        val enableFkeyResult = sqlite3_db_config(db, enableFkey)
        assertEquals(SqliteResultCode.OK, enableFkeyResult)
        assertEquals(1, outEnableFkey.value)

        val outEnableTrigger = Int32OutputParam(-1)
        val enableTrigger = SqliteDbConfigOption.ENABLE_TRIGGER(0, outEnableTrigger)
        val enableTriggerResult = sqlite3_db_config(db, enableTrigger)
        assertEquals(SqliteResultCode.OK, enableTriggerResult)
        assertEquals(0, outEnableTrigger.value)

        val outEnableFts3Tokenizer = Int32OutputParam(-1)
        
        val enableFts3Tokenizer =
            SqliteDbConfigOption.ENABLE_FTS3_TOKENIZER(1, outEnableFts3Tokenizer)
        
        val enableFts3TokenizerResult = sqlite3_db_config(db, enableFts3Tokenizer)
        assertEquals(SqliteResultCode.OK, enableFts3TokenizerResult)
        assertEquals(1, outEnableFts3Tokenizer.value)

        val outEnableLoadExtension = Int32OutputParam(-1)
        val enableLoadExtension = SqliteDbConfigOption.ENABLE_LOAD_EXTENSION(0, outEnableLoadExtension)
        val enableLoadExtensionResult = sqlite3_db_config(db, enableLoadExtension)
        assertEquals(SqliteResultCode.OK, enableLoadExtensionResult)
        assertEquals(0, outEnableLoadExtension.value)
        
        val outNoCkptOnClose = Int32OutputParam(-1)
        val noCkptOnClose = SqliteDbConfigOption.NO_CKPT_ON_CLOSE(1, outNoCkptOnClose)
        val noCkptOnCloseResult = sqlite3_db_config(db, noCkptOnClose)
        assertEquals(SqliteResultCode.OK, noCkptOnCloseResult)
        assertEquals(1, outNoCkptOnClose.value)

        val outEnableQpsg = Int32OutputParam(-1)
        val enableQpsg = SqliteDbConfigOption.ENABLE_QPSG(0, outEnableQpsg)
        val enableQpsgResult = sqlite3_db_config(db, enableQpsg)
        assertEquals(SqliteResultCode.OK, enableQpsgResult)
        assertEquals(0, outEnableQpsg.value)
        
        val outTriggerEqp = Int32OutputParam(-1)
        val triggerEqp = SqliteDbConfigOption.TRIGGER_EQP(1, outTriggerEqp)
        val triggerEqpResult = sqlite3_db_config(db, triggerEqp)
        assertEquals(SqliteResultCode.OK, triggerEqpResult)
        assertEquals(1, outTriggerEqp.value)

        val resetDatabase = SqliteDbConfigOption.RESET_DATABASE(1)
        val resetDatabaseResult = sqlite3_db_config(db, resetDatabase)
        assertEquals(SqliteResultCode.OK, resetDatabaseResult)

        val outDefensive = Int32OutputParam(-1)
        val defensive = SqliteDbConfigOption.DEFENSIVE(0, outDefensive)
        val defensiveResult = sqlite3_db_config(db, defensive)
        assertEquals(SqliteResultCode.OK, defensiveResult)
        assertEquals(0, outDefensive.value)

        val outWritableSchema = Int32OutputParam(-1)
        val writableSchema = SqliteDbConfigOption.WRITABLE_SCHEMA(1, outWritableSchema)
        val writableSchemaResult = sqlite3_db_config(db, writableSchema)
        assertEquals(SqliteResultCode.OK, writableSchemaResult)
        assertEquals(1, outWritableSchema.value)

        val outLegacyAlterTable = Int32OutputParam(-1)
        val legacyAlterTable = SqliteDbConfigOption.LEGACY_ALTER_TABLE(1, outLegacyAlterTable)
        val legacyAlterTableResult = sqlite3_db_config(db, legacyAlterTable)
        assertEquals(SqliteResultCode.OK, legacyAlterTableResult)
        assertEquals(1, outLegacyAlterTable.value)

        val outDqsDml = Int32OutputParam(-1)
        val dqsDml = SqliteDbConfigOption.DQS_DML(0, outDqsDml)
        val dqsDmlResult = sqlite3_db_config(db, dqsDml)
        assertEquals(SqliteResultCode.OK, dqsDmlResult)
        assertEquals(0, outDqsDml.value)

        val outDqsDdl = Int32OutputParam(-1)
        val dqsDdl = SqliteDbConfigOption.DQS_DDL(1, outDqsDdl)
        val dqsDdlResult = sqlite3_db_config(db, dqsDdl)
        assertEquals(SqliteResultCode.OK, dqsDdlResult)
        assertEquals(1, outDqsDdl.value)

        val outEnableView = Int32OutputParam(-1)
        val enableView = SqliteDbConfigOption.ENABLE_VIEW(0, outEnableView)
        val enableViewResult = sqlite3_db_config(db, enableView)
        assertEquals(SqliteResultCode.OK, enableViewResult)
        assertEquals(0, outEnableView.value)

        val outLegacyFileFormat = Int32OutputParam(-1)
        val legacyFileFormat = SqliteDbConfigOption.LEGACY_FILE_FORMAT(1, outLegacyFileFormat)
        val legacyFileFormatResult = sqlite3_db_config(db, legacyFileFormat)
        assertEquals(SqliteResultCode.OK, legacyFileFormatResult)
        assertEquals(1, outLegacyFileFormat.value)

        val outTrustedSchema = Int32OutputParam(-1)
        val trustedSchema = SqliteDbConfigOption.TRUSTED_SCHEMA(0, outTrustedSchema)
        val trustedSchemaResult = sqlite3_db_config(db, trustedSchema)
        assertEquals(SqliteResultCode.OK, trustedSchemaResult)
        assertEquals(0, outTrustedSchema.value)

        val outStmtScanStatus = Int32OutputParam(-1)
        val stmtScanStatus = SqliteDbConfigOption.STMT_SCANSTATUS(1, outStmtScanStatus)
        val stmtScanStatusResult = sqlite3_db_config(db, stmtScanStatus)
        assertEquals(SqliteResultCode.OK, stmtScanStatusResult)
        assertEquals(1, outStmtScanStatus.value)

        val outReverseScanOrder = Int32OutputParam(-1)
        val reverseScanOrder = SqliteDbConfigOption.REVERSE_SCANORDER(0, outReverseScanOrder)
        val reverseScanOrderResult = sqlite3_db_config(db, reverseScanOrder)
        assertEquals(SqliteResultCode.OK, reverseScanOrderResult)
        assertEquals(0, outReverseScanOrder.value)

        val outEnableAttachCreate = Int32OutputParam(-1)
        val enableAttachCreate = SqliteDbConfigOption.ENABLE_ATTACH_CREATE(1, outEnableAttachCreate)
        val enableAttachCreateResult = sqlite3_db_config(db, enableAttachCreate)
        assertEquals(SqliteResultCode.OK, enableAttachCreateResult)
        assertEquals(1, outEnableAttachCreate.value)

        val outEnableAttachWrite = Int32OutputParam(-1)
        val enableAttachWrite = SqliteDbConfigOption.ENABLE_ATTACH_WRITE(0, outEnableAttachWrite)
        val enableAttachWriteResult = sqlite3_db_config(db, enableAttachWrite)
        assertEquals(SqliteResultCode.OK, enableAttachWriteResult)
        assertEquals(0, outEnableAttachWrite.value)

        val outEnableComments = Int32OutputParam(-1)
        val enableComments = SqliteDbConfigOption.ENABLE_COMMENTS(1, outEnableComments)
        val enableCommentsResult = sqlite3_db_config(db, enableComments)
        assertEquals(SqliteResultCode.OK, enableCommentsResult)
        assertEquals(1, outEnableComments.value)

        val outFpDigits = Int32OutputParam(-1)
        val fpDigits = SqliteDbConfigOption.FP_DIGITS(23, outFpDigits)
        val fpDigitsResult = sqlite3_db_config(db, fpDigits)
        assertEquals(SqliteResultCode.OK, fpDigitsResult)
        assertEquals(23, outFpDigits.value)

        val lookasideBuffer = assertNotNull(OpaqueBuffer.allocate(128))
        val lookaside = SqliteDbConfigOption.LOOKASIDE(lookasideBuffer, 128, 2)
        val lookasideResult = sqlite3_db_config(db, lookaside)
        assertEquals(SqliteResultCode.OK, lookasideResult)
        lookasideBuffer.close()
    }
}