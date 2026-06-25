package ksqlite.capi

import ksqlite.capi.types.SqliteStmtOutputParam
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqlitePrepareFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteStatementStatusCounter
import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * Tests the statement related functions.
 */
class StatementTest {

    @Test
    fun prepareWorks() = runSqliteConnectionDataTest { db ->
        val sql = "SELECT * FROM test;"
        val outStmt = SqliteStmtOutputParam()

        val prepareResult = sqlite3_prepare_v3(db, sql, SqlitePrepareFlag.DONT_LOG, outStmt)
        assertEquals(SqliteResultCode.OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val stmtDb = assertNotNull(sqlite3_db_handle(stmt))
        assertNotSame(db, stmtDb)
        assertEquals(db, stmtDb)

        val stmtSql = sqlite3_sql(stmt)
        assertEquals(sql, stmtSql)

        val columnCount = sqlite3_column_count(stmt)
        assertEquals(5, columnCount)

        val isReadOnly = sqlite3_stmt_readonly(stmt)
        assertEquals(1, isReadOnly)

        val stepResult = sqlite3_step(stmt)
        assertEquals(SqliteResultCode.DONE, stepResult)

        val isBusy = sqlite3_stmt_busy(stmt)
        assertEquals(0, isBusy)

        val initExplainMode = sqlite3_stmt_isexplain(stmt)
        assertEquals(SqliteExplainMode.NORMAL, initExplainMode)

        val setExplainModeResult = sqlite3_stmt_explain(stmt, SqliteExplainMode.EXPLAIN_QUERY_PLAN)
        assertEquals(SqliteResultCode.BUSY, setExplainModeResult)

        val resetResult = sqlite3_reset(stmt)
        assertEquals(SqliteResultCode.OK, resetResult)

        val setExplainModeResult2 = sqlite3_stmt_explain(stmt, SqliteExplainMode.EXPLAIN_QUERY_PLAN)
        assertEquals(SqliteResultCode.OK, setExplainModeResult2)

        val updatedExplainMode = sqlite3_stmt_isexplain(stmt)
        assertEquals(SqliteExplainMode.EXPLAIN_QUERY_PLAN, updatedExplainMode)

        val runCount = sqlite3_stmt_status(stmt, SqliteStatementStatusCounter.RUN, 0)
        assertEquals(1, runCount)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(SqliteResultCode.OK, finalizeResult)
    }

    /*@Test
    fun prepareBufferWorks() = runSqliteConnectionDataTest { db ->
        val baseSql = "SELECT * FROM test;"
        val doubledSql = baseSql + baseSql
        val sqlBytes = doubledSql.encodeToByteArray()
        assertEquals("SELECT * FROM test;SELECT * FROM test;".encodeToByteArray(), sql)

        val outStmt = SqliteStmtOutputParam()

        val prepareResult = sqlite3_prepare_v3(db, sqlBytes, sqlBytes.size, null, outStmt)
        assertEquals(SqliteResultCode.OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val stmtDb = assertNotNull(sqlite3_db_handle(stmt))
        assertNotSame(db, stmtDb)
        assertEquals(db, stmtDb)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(SqliteResultCode.OK, finalizeResult)
    }*/

    @Test
    fun bindingWorks() = runSqliteConnectionDataTest { db ->
        val sql = "INSERT INTO test VALUES (:theInt, ?, ?, ?, @zeroBlob);"
        val outStmt = SqliteStmtOutputParam()

        val prepareResult = sqlite3_prepare_v2(db, sql, outStmt)
        assertEquals(SqliteResultCode.OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val paramCount = sqlite3_bind_parameter_count(stmt)
        assertEquals(5, paramCount)

        val param1Name = sqlite3_bind_parameter_name(stmt, 1)
        assertEquals(":theInt", param1Name)

        val zeroBlobParamIndex = sqlite3_bind_parameter_index(stmt, "@zeroBlob")
        assertEquals(5, zeroBlobParamIndex)

        val initExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (NULL, NULL, NULL, NULL, NULL);",
            initExpandedSql
        )

        val bindIntResult = sqlite3_bind_int(stmt, 1, 2)
        assertEquals(SqliteResultCode.OK, bindIntResult)

        val bindDoubleResult = sqlite3_bind_double(stmt, 2, 5.01)
        assertEquals(SqliteResultCode.OK, bindDoubleResult)

        val bindTextResult = sqlite3_bind_text(stmt, 3, "Kiwi")
        assertEquals(SqliteResultCode.OK, bindTextResult)

        val blob = ByteArray(2) { it.toByte() }
        var blobDestructorCalled = false

        val bindBlobResult = sqlite3_bind_blob(stmt, 4, blob, blob.size) {
            blobDestructorCalled = true
        }

        assertEquals(SqliteResultCode.OK, bindBlobResult)

        val bindZeroBlobResult = sqlite3_bind_zeroblob(stmt, 5, 3)
        assertEquals(SqliteResultCode.OK, bindZeroBlobResult)

        val boundExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (2, 5.01, 'Kiwi', x'0001', zeroblob(3));",
            boundExpandedSql
        )

        val bindNullResult = sqlite3_bind_null(stmt, 5)
        assertEquals(SqliteResultCode.OK, bindNullResult)

        val nullExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (2, 5.01, 'Kiwi', x'0001', NULL);",
            nullExpandedSql
        )

        val clearResult = sqlite3_clear_bindings(stmt)
        assertEquals(SqliteResultCode.OK, clearResult)
        assertTrue(blobDestructorCalled)

        val clearedExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (NULL, NULL, NULL, NULL, NULL);",
            clearedExpandedSql
        )

        val bindInt64Result = sqlite3_bind_int64(stmt, 1, 500L)
        assertEquals(SqliteResultCode.OK, bindInt64Result)

        val text = "Poire".encodeToByteArray()
        val textBuffer = assertNotNull(sqlite3_malloc(text.size))
        textBuffer.write(text)

        var textBufferDestructorCalled = false

        val bindText64Result =
            sqlite3_bind_text64(stmt, 3, textBuffer, textBuffer.byteSize, SqliteTextEncoding.UTF8) {
                textBufferDestructorCalled = true
            }

        assertEquals(SqliteResultCode.OK, bindText64Result)

        val blobBuffer = assertNotNull(sqlite3_malloc(blob.size))
        blobBuffer.write(blob)

        var blobBufferDestructorCalled = false

        val bindBlob64Result = sqlite3_bind_blob64(stmt, 4, blobBuffer, blobBuffer.byteSize) {
            blobBufferDestructorCalled = true
        }

        assertEquals(SqliteResultCode.OK, bindBlob64Result)

        val bindZeroBlob64Result = sqlite3_bind_zeroblob64(stmt, 5, 948948489UL)
        assertEquals(SqliteResultCode.OK, bindZeroBlob64Result)

        val bound64ExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (500, NULL, 'Poire', x'0001', zeroblob(948948489));",
            bound64ExpandedSql
        )

        val cleanupResult = sqlite3_clear_bindings(stmt)
        assertEquals(SqliteResultCode.OK, cleanupResult)
        assertTrue(textBufferDestructorCalled)
        assertTrue(blobBufferDestructorCalled)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(SqliteResultCode.OK, finalizeResult)
    }

    @Test
    fun columnWorks() = runSqliteConnectionDataTest { db ->

    }
}