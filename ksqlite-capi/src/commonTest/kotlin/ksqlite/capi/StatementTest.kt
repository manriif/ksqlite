/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.capi

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.readBytesOrThrow
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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
        val outStmt = sqlite3_stmt.OutputParam()

        val prepareResult = sqlite3_prepare_v3(db, sql, DONT_LOG, outStmt)
        assertEquals(OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val stmtDb = assertNotNull(sqlite3_db_handle(stmt))
        assertNotSame(db, stmtDb)
        assertEquals(db, stmtDb)

        val stmtSql = sqlite3_sql(stmt)
        assertEquals(sql, stmtSql)

        val columnCount = sqlite3_column_count(stmt)
        assertEquals(5, columnCount)

        val col0DbName = sqlite3_column_database_name(stmt, 0)
        assertEquals("main", col0DbName)

        val col0DeclType = sqlite3_column_decltype(stmt, 0)
        assertEquals("INTEGER", col0DeclType)

        val col3Name = sqlite3_column_name(stmt, 3)
        assertEquals("blob_t", col3Name)

        val col2OriginName = sqlite3_column_origin_name(stmt, 2)
        assertEquals("text_t", col2OriginName)

        val col4TableName = sqlite3_column_table_name(stmt, 4)
        assertEquals("test", col4TableName)

        val isReadOnly = sqlite3_stmt_readonly(stmt)
        assertEquals(1, isReadOnly)

        val stepResult = sqlite3_step(stmt)
        assertEquals(DONE, stepResult)

        val isBusy = sqlite3_stmt_busy(stmt)
        assertEquals(0, isBusy)

        val initExplainMode = sqlite3_stmt_isexplain(stmt)
        assertEquals(NORMAL, initExplainMode)

        val setExplainModeResult = sqlite3_stmt_explain(stmt, EXPLAIN_QUERY_PLAN)
        assertEquals(BUSY, setExplainModeResult)

        val resetResult = sqlite3_reset(stmt)
        assertEquals(OK, resetResult)

        val setExplainModeResult2 = sqlite3_stmt_explain(stmt, EXPLAIN)
        assertEquals(OK, setExplainModeResult2)

        val updatedExplainMode = sqlite3_stmt_isexplain(stmt)
        assertEquals(EXPLAIN, updatedExplainMode)

        val runCount = sqlite3_stmt_status(stmt, RUN, 0)
        assertEquals(1, runCount)

        val nextStmt = sqlite3_next_stmt(db, null)
        assertEquals(nextStmt, stmt)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(OK, finalizeResult)
    }

    fun prepareBufferTest(
        prepare: (
            db: sqlite3,
            sql: ByteArray,
            outStmt: sqlite3_stmt.OutputParam,
            outOffset: Int32OutputParam
        ) -> SqliteResultCode
    ) = runSqliteConnectionDataTest { db ->
        val baseSql = "SELECT * FROM test;"
        val doubledSql = baseSql + baseSql
        assertEquals("SELECT * FROM test;SELECT * FROM test;", doubledSql)

        val sql = doubledSql.encodeToByteArray()
        val outStmt = sqlite3_stmt.OutputParam()
        val outOffset = Int32OutputParam()

        val prepareResult = prepare(db, sql, outStmt, outOffset)
        assertEquals(OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val expectedOffset = sql.size / 2
        assertEquals(expectedOffset, outOffset.value)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(OK, finalizeResult)
    }

    @Test
    fun prepareBufferV3Works() = prepareBufferTest { db, sql, outStmt, outOffset ->
        sqlite3_prepare_v3(db, sql, sql.size, null, outStmt, outOffset)
    }

    @Test
    fun prepareBufferV2Works() = prepareBufferTest { db, sql, outStmt, outOffset ->
        sqlite3_prepare_v2(db, sql, sql.size, outStmt, outOffset)
    }

    @Test
    fun bindingWorks() = runSqliteConnectionDataTest { db ->
        val sql = "INSERT INTO test VALUES (:theInt, ?, ?, ?, @zeroBlob);"
        val outStmt = sqlite3_stmt.OutputParam()

        val prepareResult = sqlite3_prepare_v2(db, sql, outStmt)
        assertEquals(OK, prepareResult)

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
        assertEquals(OK, bindIntResult)

        val bindDoubleResult = sqlite3_bind_double(stmt, 2, 5.01)
        assertEquals(OK, bindDoubleResult)

        val bindTextResult = sqlite3_bind_text(stmt, 3, "Kiwi")
        assertEquals(OK, bindTextResult)

        val blob = ByteArray(2) { it.toByte() }
        var blobDestructorCalled = false

        val bindBlobResult = sqlite3_bind_blob(stmt, 4, blob, blob.size) {
            blobDestructorCalled = true
        }

        assertEquals(OK, bindBlobResult)

        val bindZeroBlobResult = sqlite3_bind_zeroblob(stmt, 5, 3)
        assertEquals(OK, bindZeroBlobResult)

        val boundExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (2, 5.01, 'Kiwi', x'0001', zeroblob(3));",
            boundExpandedSql
        )

        val bindNullResult = sqlite3_bind_null(stmt, 5)
        assertEquals(OK, bindNullResult)

        val nullExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (2, 5.01, 'Kiwi', x'0001', NULL);",
            nullExpandedSql
        )

        val clearResult = sqlite3_clear_bindings(stmt)
        assertEquals(OK, clearResult)
        assertTrue(blobDestructorCalled)

        val clearedExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (NULL, NULL, NULL, NULL, NULL);",
            clearedExpandedSql
        )

        val bindInt64Result = sqlite3_bind_int64(stmt, 1, 500L)
        assertEquals(OK, bindInt64Result)

        val text = "Poire".encodeToByteArray()
        val textBuffer = assertNotNull(sqlite3_malloc(text.size))
        textBuffer.write(text)

        var textBufferDestructorCalled = false

        val bindText64Result = sqlite3_bind_text64(stmt, 3, textBuffer, textBuffer.byteSize, UTF8) {
            textBufferDestructorCalled = true
        }

        assertEquals(OK, bindText64Result)

        val blobBuffer = assertNotNull(sqlite3_malloc(blob.size))
        blobBuffer.write(blob)

        var blobBufferDestructorCalled = false

        val bindBlob64Result = sqlite3_bind_blob64(stmt, 4, blobBuffer, blobBuffer.byteSize) {
            blobBufferDestructorCalled = true
        }

        assertEquals(OK, bindBlob64Result)

        val bindZeroBlob64Result = sqlite3_bind_zeroblob64(stmt, 5, 948948489UL)
        assertEquals(OK, bindZeroBlob64Result)

        val bound64ExpandedSql = sqlite3_expanded_sql(stmt)
        assertEquals(
            "INSERT INTO test VALUES (500, NULL, 'Poire', x'0001', zeroblob(948948489));",
            bound64ExpandedSql
        )

        val cleanupResult = sqlite3_clear_bindings(stmt)
        assertEquals(OK, cleanupResult)
        assertTrue(textBufferDestructorCalled)
        assertTrue(blobBufferDestructorCalled)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(OK, finalizeResult)
    }

    @Test
    fun steppingWorks() = runSqliteConnectionDataTest { db ->
        val insertSql = """
            INSERT INTO test VALUES 
                (18, 36.85, 'Pêche', x'051B', zeroblob(6)), 
                (623, NULL, NULL, NULL, NULL); 
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        val selectSql = "SELECT * FROM test;"
        val outStmt = sqlite3_stmt.OutputParam()

        val prepareResult = sqlite3_prepare_v2(db, selectSql, outStmt)
        assertEquals(OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val step1Result = sqlite3_step(stmt)
        assertEquals(ROW, step1Result)

        val dataCount = sqlite3_data_count(stmt)
        assertEquals(5, dataCount)

        val col0Type = sqlite3_column_type(stmt, 0)
        assertEquals(INTEGER, col0Type)

        val col0Value = sqlite3_column_int(stmt, 0)
        assertEquals(18, col0Value)

        val col1Value = sqlite3_column_double(stmt, 1)
        assertEquals(36.85, col1Value)

        val col2Value = sqlite3_column_text(stmt, 2)
        assertEquals("Pêche", col2Value)

        val col3Value = sqlite3_column_blob(stmt, 3)
        val col3ValueExpected = byteArrayOf(5, 27)
        assertContentEquals(col3ValueExpected, col3Value)

        val col4Size = sqlite3_column_bytes(stmt, 4)
        assertEquals(6, col4Size)

        val col4Buffer = assertNotNull(sqlite3_column_buffer(stmt, 4))
        val col4Value = col4Buffer.readBytesOrThrow()
        val col4ValueExpected = byteArrayOf(0, 0, 0, 0, 0, 0)
        assertContentEquals(col4ValueExpected, col4Value)

        val step2Result = sqlite3_step(stmt)
        assertEquals(ROW, step2Result)

        val col0Value64 = sqlite3_column_int64(stmt, 0)
        assertEquals(623L, col0Value64)

        val col1Type = sqlite3_column_type(stmt, 1)
        assertEquals(NULL, col1Type)

        // Value API is tested somewhere else
        val value = sqlite3_column_value(stmt, 0)
        assertNotNull(value)

        val step3Result = sqlite3_step(stmt)
        assertEquals(DONE, step3Result)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(OK, finalizeResult)
    }
}