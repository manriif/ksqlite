package ksqlite.capi

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.OpaqueBuffer
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.SqliteFileControlOpcode
import ksqlite.capi.types.SqliteSerializeResult
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the connection related functions.
 */
class ConnectionTest {

    @Test
    fun connectionOpens() = runSqliteTest {
        val outDb = sqlite3.OutputParam()

        val openResult = sqlite3_open(":memory:", outDb)
        assertEquals(SqliteResultCode.OK, openResult)

        val db = assertNotNull(outDb.value)

        val name = sqlite3_db_name(db, 1)
        assertEquals("temp", name)

        val fileName = sqlite3_db_filename(db, "main")
        assertEquals("", fileName)

        val defaultLengthLimit = sqlite3_limit(db, LENGTH, 100_000)
        assertEquals(1_000_000_000, defaultLengthLimit)

        val newLengthLimit = sqlite3_limit(db, LENGTH, -1)
        assertEquals(100_000, newLengthLimit)

        val closeResult = sqlite3_close_v2(db)
        assertEquals(OK, closeResult)
    }

    @Test
    fun errorApisWorks() = runSqliteConnectionTest { db ->
        val outError = Utf8OutputParam()
        val result = sqlite3_exec(db, "CREATE table fail;", outError, null, null)

        assertEquals(ERROR, result)

        val expectedErrorMessage = """near ";": syntax error"""
        assertEquals(expectedErrorMessage, outError.value)

        val errorMessage = sqlite3_errmsg(db)
        assertEquals(expectedErrorMessage, errorMessage)

        val errorOffset = sqlite3_error_offset(db)
        assertEquals(17, errorOffset)

        val errorCode = sqlite3_errcode(db)
        assertEquals(ERROR, errorCode)
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

        assertEquals(OK, result)

        val transactionState = sqlite3_txn_state(db, null)
        assertEquals(NONE, transactionState)

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
        assertEquals(OK, result)

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

        assertEquals(OK, metadataResult)
        assertEquals("INTEGER", outDataType.value)
        assertEquals("BINARY", outCollationSequence.value)
        assertEquals(1, outNotNull.value)
        assertEquals(1, outPrimaryKey.value)
        assertEquals(1, outAutoIncrement.value)
    }

    @Test
    fun serializationWorks() = runSqliteTest {
        // Serialize
        val outSerializeDb = sqlite3.OutputParam()

        val serializeOpenResult = sqlite3_open(":memory:", outSerializeDb)
        assertEquals(OK, serializeOpenResult)

        val serializeDb = assertNotNull(outSerializeDb.value)

        val insertSql = """
            CREATE TABLE fruits(id INTEGER, name TEXT);
            INSERT INTO fruits VALUES (56, 'Framboise');
        """.trimIndent()

        val insertResult = sqlite3_exec(serializeDb, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        val inMemoryDb = sqlite3_serialize(serializeDb, null, NOCOPY)
        assertIs<SqliteSerializeResult.Failure>(inMemoryDb)

        val copiedDb = sqlite3_serialize(serializeDb, null, null)
        assertIs<SqliteSerializeResult.Mutable>(copiedDb)

        val closeSerializeResult = sqlite3_close_v2(serializeDb)
        assertEquals(OK, closeSerializeResult)

        // Deserialize
        val outDeserializeDb = sqlite3.OutputParam()

        val deserializeOpenResult = sqlite3_open(":memory:", outDeserializeDb)
        assertEquals(OK, deserializeOpenResult)

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

        assertEquals(OK, deserializeResult)

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

        assertEquals(OK, readResult)
        assertTrue(callbackCalled)

        val writeSql = "INSERT INTO fruits VALUES (10, 'Mangue');"

        val writeResult = sqlite3_exec(deserializeDb, writeSql, null, null, null)
        assertEquals(READONLY, writeResult)

        val closeDeserializeResult = sqlite3_close_v2(deserializeDb)
        assertEquals(OK, closeDeserializeResult)
    }

    @Test
    fun configWorks() = runSqliteConnectionTest { db ->
        val actualName = sqlite3_db_name(db, 0)
        assertEquals("main", actualName)

        val newName = "theMainDatabase"
        val mainDbName = SqliteDbConfigOption.MAINDBNAME(newName)
        val mainDbNameResult = sqlite3_db_config(db, mainDbName)
        assertEquals(OK, mainDbNameResult)

        val updatedName = sqlite3_db_name(db, 0)
        assertEquals(newName, updatedName)

        val outEnableFkey = Int32OutputParam(-1)
        val enableFkey = SqliteDbConfigOption.ENABLE_FKEY(1, outEnableFkey)
        val enableFkeyResult = sqlite3_db_config(db, enableFkey)
        assertEquals(OK, enableFkeyResult)
        assertEquals(1, outEnableFkey.value)

        val outEnableTrigger = Int32OutputParam(-1)
        val enableTrigger = SqliteDbConfigOption.ENABLE_TRIGGER(0, outEnableTrigger)
        val enableTriggerResult = sqlite3_db_config(db, enableTrigger)
        assertEquals(OK, enableTriggerResult)
        assertEquals(0, outEnableTrigger.value)

        val outEnableFts3Tokenizer = Int32OutputParam(-1)

        val enableFts3Tokenizer =
            SqliteDbConfigOption.ENABLE_FTS3_TOKENIZER(1, outEnableFts3Tokenizer)

        val enableFts3TokenizerResult = sqlite3_db_config(db, enableFts3Tokenizer)
        assertEquals(OK, enableFts3TokenizerResult)
        assertEquals(1, outEnableFts3Tokenizer.value)

        val outEnableLoadExtension = Int32OutputParam(-1)
        val enableLoadExtension =
            SqliteDbConfigOption.ENABLE_LOAD_EXTENSION(0, outEnableLoadExtension)
        val enableLoadExtensionResult = sqlite3_db_config(db, enableLoadExtension)
        assertEquals(OK, enableLoadExtensionResult)
        assertEquals(0, outEnableLoadExtension.value)

        val outNoCkptOnClose = Int32OutputParam(-1)
        val noCkptOnClose = SqliteDbConfigOption.NO_CKPT_ON_CLOSE(1, outNoCkptOnClose)
        val noCkptOnCloseResult = sqlite3_db_config(db, noCkptOnClose)
        assertEquals(OK, noCkptOnCloseResult)
        assertEquals(1, outNoCkptOnClose.value)

        val outEnableQpsg = Int32OutputParam(-1)
        val enableQpsg = SqliteDbConfigOption.ENABLE_QPSG(0, outEnableQpsg)
        val enableQpsgResult = sqlite3_db_config(db, enableQpsg)
        assertEquals(SqliteResultCode.OK, enableQpsgResult)
        assertEquals(0, outEnableQpsg.value)

        val outTriggerEqp = Int32OutputParam(-1)
        val triggerEqp = SqliteDbConfigOption.TRIGGER_EQP(1, outTriggerEqp)
        val triggerEqpResult = sqlite3_db_config(db, triggerEqp)
        assertEquals(OK, triggerEqpResult)
        assertEquals(1, outTriggerEqp.value)

        val resetDatabase = SqliteDbConfigOption.RESET_DATABASE(1)
        val resetDatabaseResult = sqlite3_db_config(db, resetDatabase)
        assertEquals(OK, resetDatabaseResult)

        val outDefensive = Int32OutputParam(-1)
        val defensive = SqliteDbConfigOption.DEFENSIVE(0, outDefensive)
        val defensiveResult = sqlite3_db_config(db, defensive)
        assertEquals(OK, defensiveResult)
        assertEquals(0, outDefensive.value)

        val outWritableSchema = Int32OutputParam(-1)
        val writableSchema = SqliteDbConfigOption.WRITABLE_SCHEMA(1, outWritableSchema)
        val writableSchemaResult = sqlite3_db_config(db, writableSchema)
        assertEquals(OK, writableSchemaResult)
        assertEquals(1, outWritableSchema.value)

        val outLegacyAlterTable = Int32OutputParam(-1)
        val legacyAlterTable = SqliteDbConfigOption.LEGACY_ALTER_TABLE(1, outLegacyAlterTable)
        val legacyAlterTableResult = sqlite3_db_config(db, legacyAlterTable)
        assertEquals(OK, legacyAlterTableResult)
        assertEquals(1, outLegacyAlterTable.value)

        val outDqsDml = Int32OutputParam(-1)
        val dqsDml = SqliteDbConfigOption.DQS_DML(0, outDqsDml)
        val dqsDmlResult = sqlite3_db_config(db, dqsDml)
        assertEquals(OK, dqsDmlResult)
        assertEquals(0, outDqsDml.value)

        val outDqsDdl = Int32OutputParam(-1)
        val dqsDdl = SqliteDbConfigOption.DQS_DDL(1, outDqsDdl)
        val dqsDdlResult = sqlite3_db_config(db, dqsDdl)
        assertEquals(OK, dqsDdlResult)
        assertEquals(1, outDqsDdl.value)

        val outEnableView = Int32OutputParam(-1)
        val enableView = SqliteDbConfigOption.ENABLE_VIEW(0, outEnableView)
        val enableViewResult = sqlite3_db_config(db, enableView)
        assertEquals(OK, enableViewResult)
        assertEquals(0, outEnableView.value)

        val outLegacyFileFormat = Int32OutputParam(-1)
        val legacyFileFormat = SqliteDbConfigOption.LEGACY_FILE_FORMAT(1, outLegacyFileFormat)
        val legacyFileFormatResult = sqlite3_db_config(db, legacyFileFormat)
        assertEquals(OK, legacyFileFormatResult)
        assertEquals(1, outLegacyFileFormat.value)

        val outTrustedSchema = Int32OutputParam(-1)
        val trustedSchema = SqliteDbConfigOption.TRUSTED_SCHEMA(0, outTrustedSchema)
        val trustedSchemaResult = sqlite3_db_config(db, trustedSchema)
        assertEquals(OK, trustedSchemaResult)
        assertEquals(0, outTrustedSchema.value)

        val outStmtScanStatus = Int32OutputParam(-1)
        val stmtScanStatus = SqliteDbConfigOption.STMT_SCANSTATUS(1, outStmtScanStatus)
        val stmtScanStatusResult = sqlite3_db_config(db, stmtScanStatus)
        assertEquals(OK, stmtScanStatusResult)
        assertEquals(1, outStmtScanStatus.value)

        val outReverseScanOrder = Int32OutputParam(-1)
        val reverseScanOrder = SqliteDbConfigOption.REVERSE_SCANORDER(0, outReverseScanOrder)
        val reverseScanOrderResult = sqlite3_db_config(db, reverseScanOrder)
        assertEquals(OK, reverseScanOrderResult)
        assertEquals(0, outReverseScanOrder.value)

        val outEnableAttachCreate = Int32OutputParam(-1)
        val enableAttachCreate = SqliteDbConfigOption.ENABLE_ATTACH_CREATE(1, outEnableAttachCreate)
        val enableAttachCreateResult = sqlite3_db_config(db, enableAttachCreate)
        assertEquals(OK, enableAttachCreateResult)
        assertEquals(1, outEnableAttachCreate.value)

        val outEnableAttachWrite = Int32OutputParam(-1)
        val enableAttachWrite = SqliteDbConfigOption.ENABLE_ATTACH_WRITE(0, outEnableAttachWrite)
        val enableAttachWriteResult = sqlite3_db_config(db, enableAttachWrite)
        assertEquals(OK, enableAttachWriteResult)
        assertEquals(0, outEnableAttachWrite.value)

        val outEnableComments = Int32OutputParam(-1)
        val enableComments = SqliteDbConfigOption.ENABLE_COMMENTS(1, outEnableComments)
        val enableCommentsResult = sqlite3_db_config(db, enableComments)
        assertEquals(OK, enableCommentsResult)
        assertEquals(1, outEnableComments.value)

        val outFpDigits = Int32OutputParam(-1)
        val fpDigits = SqliteDbConfigOption.FP_DIGITS(23, outFpDigits)
        val fpDigitsResult = sqlite3_db_config(db, fpDigits)
        assertEquals(OK, fpDigitsResult)
        assertEquals(23, outFpDigits.value)

        val lookasideBuffer = assertNotNull(OpaqueBuffer.allocate(128))
        val lookaside = SqliteDbConfigOption.LOOKASIDE(lookasideBuffer, 128, 2)
        val lookasideResult = sqlite3_db_config(db, lookaside)
        assertEquals(OK, lookasideResult)
        lookasideBuffer.close()
    }

    @Test
    fun fileControlWorks() = runSqliteConnectionTest { db ->
        val allowedResultCodes = arrayOf<SqliteResultCode>(OK, NOTFOUND)

        val outLastErrno = Int32OutputParam(-1)
        val lastErrno = SqliteFileControlOpcode.LAST_ERRNO(outLastErrno)
        val lastErrnoResult = sqlite3_file_control(db, null, lastErrno)
        assertContains(allowedResultCodes, lastErrnoResult)

        if (lastErrnoResult != NOTFOUND) {
            assertNotEquals(-1, outLastErrno.value)
        }

        val outSizeHint = Int64OutputParam(-1)
        val sizeHint = SqliteFileControlOpcode.SIZE_HINT(outSizeHint)
        val sizeHintResult = sqlite3_file_control(db, null, sizeHint)
        assertContains(allowedResultCodes, sizeHintResult)

        if (sizeHintResult != NOTFOUND) {
            assertNotEquals(-1, outSizeHint.value)
        }

        val outChunkSize = Int32OutputParam(-1)
        val chunkSize = SqliteFileControlOpcode.CHUNK_SIZE(outChunkSize)
        val chunkSizeResult = sqlite3_file_control(db, null, chunkSize)
        assertContains(allowedResultCodes, chunkSizeResult)

        if (lastErrnoResult != NOTFOUND) {
            assertNotEquals(-1, outChunkSize.value)
        }

        val outPersistWal = Int32OutputParam(-1)
        val persistWal = SqliteFileControlOpcode.PERSIST_WAL(outPersistWal)
        val persistWalResult = sqlite3_file_control(db, null, persistWal)
        assertContains(allowedResultCodes, persistWalResult)

        if (persistWalResult != NOTFOUND) {
            assertNotEquals(-1, outPersistWal.value)
        }

        val outOverwrite = Int64OutputParam(-1)
        val overwrite = SqliteFileControlOpcode.OVERWRITE(outOverwrite)
        val overwriteResult = sqlite3_file_control(db, null, overwrite)
        assertContains(allowedResultCodes, overwriteResult)

        if (overwriteResult != NOTFOUND) {
            assertNotEquals(-1, outOverwrite.value)
        }

        val outVfsName = Utf8OutputParam()
        val vfsName = SqliteFileControlOpcode.VFSNAME(outVfsName)
        val vfsNameResult = sqlite3_file_control(db, null, vfsName)
        assertContains(allowedResultCodes, vfsNameResult)

        if (vfsNameResult != NOTFOUND) {
            assertNotNull(outVfsName.value)
        }

        val outPowerSafeOverwrite = Int32OutputParam(-1)
        val powerSafeOverwrite = SqliteFileControlOpcode.POWERSAFE_OVERWRITE(outPowerSafeOverwrite)
        val powerSafeOverwriteResult = sqlite3_file_control(db, null, powerSafeOverwrite)
        assertContains(allowedResultCodes, powerSafeOverwriteResult)

        if (powerSafeOverwriteResult != NOTFOUND) {
            assertNotEquals(-1, outPowerSafeOverwrite.value)
        }

        val outTempFileName = Utf8OutputParam()
        val tempFileName = SqliteFileControlOpcode.TEMPFILENAME(outTempFileName)
        val tempFileNameResult = sqlite3_file_control(db, null, tempFileName)
        assertContains(allowedResultCodes, tempFileNameResult)

        if (tempFileNameResult != NOTFOUND) {
            assertNotNull(outTempFileName.value)
        }

        val outMmapSize = Int64OutputParam(-1)
        val mmapSize = SqliteFileControlOpcode.MMAP_SIZE(outMmapSize)
        val mmapSizeResult = sqlite3_file_control(db, null, mmapSize)
        assertContains(allowedResultCodes, mmapSizeResult)

        if (mmapSizeResult != NOTFOUND) {
            assertNotEquals(-1, outMmapSize.value)
        }

        val outHasMoved = Int32OutputParam(-1)
        val hasMoved = SqliteFileControlOpcode.HAS_MOVED(outHasMoved)
        val hasMovedResult = sqlite3_file_control(db, null, hasMoved)
        assertContains(allowedResultCodes, hasMovedResult)

        if (hasMovedResult != NOTFOUND) {
            assertNotEquals(-1, outHasMoved.value)
        }

        val outVfsPointer = sqlite3_vfs.OutputParam()
        val vfsPointer = SqliteFileControlOpcode.VFS_POINTER(outVfsPointer)
        val vfsPointerResult = sqlite3_file_control(db, null, vfsPointer)
        assertEquals(OK, vfsPointerResult)
        assertNotNull(outVfsPointer.value)

        val outLockTimeout = Int32OutputParam(-1)
        val lockTimeout = SqliteFileControlOpcode.LOCK_TIMEOUT(outLockTimeout)
        val lockTimeoutResult = sqlite3_file_control(db, null, lockTimeout)
        assertContains(allowedResultCodes, lockTimeoutResult)

        if (lockTimeoutResult != NOTFOUND) {
            assertNotEquals(-1, outLockTimeout.value)
        }

        val outDataVersion = Int32OutputParam(-1)
        val dataVersion = SqliteFileControlOpcode.DATA_VERSION(outDataVersion)
        val dataVersionResult = sqlite3_file_control(db, null, dataVersion)
        assertEquals(OK, dataVersionResult)
        assertNotEquals(-1, outDataVersion.value)

        val outSizeLimit = Int64OutputParam(-1)
        val sizeLimit = SqliteFileControlOpcode.SIZE_LIMIT(outSizeLimit)
        val sizeLimitResult = sqlite3_file_control(db, null, sizeLimit)
        assertContains(allowedResultCodes, sizeLimitResult)

        if (sizeLimitResult != NOTFOUND) {
            assertNotEquals(-1, outSizeLimit.value)
        }

        val outReserveBytes = Int32OutputParam(-1)
        val reserveBytes = SqliteFileControlOpcode.RESERVE_BYTES(outReserveBytes)
        val reserveBytesResult = sqlite3_file_control(db, null, reserveBytes)
        assertEquals(OK, reserveBytesResult)
        assertNotEquals(-1, outReserveBytes.value)

        val resetCacheResult = sqlite3_file_control(db, null, RESET_CACHE)
        assertEquals(OK, resetCacheResult)
    }
}