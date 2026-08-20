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

import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Regression tests for a bug on the Android JNI backend which wasn't handling empty string
 * correctly.
 */
class EmptyStringTest {

    @Test
    fun bindTextWithEmptyStringWorks() = runSqliteConnectionDataTest { db ->
        val sql = "INSERT INTO test VALUES (?, ?, ?, ?, ?);"
        val outStmt = sqlite3_stmt.OutputParam()

        val prepareResult = sqlite3_prepare_v2(db, sql, outStmt)
        assertEquals(OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val bindTextResult = sqlite3_bind_text(stmt, 3, "")
        assertEquals(OK, bindTextResult)

        // Must show up as an empty string literal, not NULL.
        val expandedSql = sqlite3_expanded_sql(stmt)
        assertEquals("INSERT INTO test VALUES (NULL, NULL, '', NULL, NULL);", expandedSql)

        val stepResult = sqlite3_step(stmt)
        assertEquals(DONE, stepResult)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(OK, finalizeResult)
    }

    @Test
    fun bindTextWithEmptyStringRoundTripsThroughAQuery() = runSqliteConnectionDataTest { db ->
        val insertSql = "INSERT INTO test (text_t) VALUES (?);"
        val outInsertStmt = sqlite3_stmt.OutputParam()

        val insertPrepareResult = sqlite3_prepare_v2(db, insertSql, outInsertStmt)
        assertEquals(OK, insertPrepareResult)

        val insertStmt = assertNotNull(outInsertStmt.value)

        val bindResult = sqlite3_bind_text(insertStmt, 1, "")
        assertEquals(OK, bindResult)

        val insertStepResult = sqlite3_step(insertStmt)
        assertEquals(DONE, insertStepResult)

        val insertFinalizeResult = sqlite3_finalize(insertStmt)
        assertEquals(OK, insertFinalizeResult)

        val outSelectStmt = sqlite3_stmt.OutputParam()
        val selectPrepareResult = sqlite3_prepare_v2(db, "SELECT text_t FROM test;", outSelectStmt)
        assertEquals(OK, selectPrepareResult)

        val selectStmt = assertNotNull(outSelectStmt.value)

        val selectStepResult = sqlite3_step(selectStmt)
        assertEquals(ROW, selectStepResult)

        // A stored NULL (what the bug would silently produce, absent the crash) is a different
        // SQLite storage class than a stored empty string - assert the type as well as the value.
        val columnType = sqlite3_column_type(selectStmt, 0)
        assertEquals(TEXT, columnType)

        val columnValue = sqlite3_column_text(selectStmt, 0)
        assertEquals("", columnValue)

        val selectFinalizeResult = sqlite3_finalize(selectStmt)
        assertEquals(OK, selectFinalizeResult)
    }

    @Test
    fun resultTextWithEmptyStringWorks() = runSqliteConnectionTest { db ->
        val createResult = sqlite3_create_function(
            db = db,
            name = "empty_text",
            nArg = 0,
            encoding = SqliteTextEncoding.UTF8,
            appData = Unit,
            func = { _, context, _ -> sqlite3_result_text(context, "") },
            step = null,
            final = null,
        )
        assertEquals(OK, createResult)

        val outStmt = sqlite3_stmt.OutputParam()
        val prepareResult = sqlite3_prepare_v2(db, "SELECT empty_text();", outStmt)
        assertEquals(OK, prepareResult)

        val stmt = assertNotNull(outStmt.value)

        val stepResult = sqlite3_step(stmt)
        assertEquals(ROW, stepResult)

        val columnType = sqlite3_column_type(stmt, 0)
        assertEquals(TEXT, columnType)

        val columnValue = sqlite3_column_text(stmt, 0)
        assertEquals("", columnValue)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(OK, finalizeResult)
    }
}
