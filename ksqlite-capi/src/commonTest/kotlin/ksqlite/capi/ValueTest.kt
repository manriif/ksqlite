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

import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests the sqlite3_value related APIs.
 */
class ValueTest {

    private fun stepOnce(
        db: sqlite3,
        sql: String,
        bind: (sqlite3_stmt) -> Unit = {},
        block: (sqlite3_stmt) -> Unit,
    ) {
        val outStmt = sqlite3_stmt.OutputParam()
        assertEquals(OK, sqlite3_prepare_v2(db, sql, outStmt))
        val stmt = assertNotNull(outStmt.value)

        bind(stmt)

        val stepResult = sqlite3_step(stmt)
        assertEquals(ROW, stepResult)

        block(stmt)

        val finalizeResult = sqlite3_finalize(stmt)
        assertEquals(OK, finalizeResult)
    }

    @Test
    fun valueGettersWorkForBoundParameters() = runSqliteConnectionTest { db ->
        stepOnce(
            db,
            "SELECT ?, ?, ?, ?, ?;",
            bind = { stmt ->
                assertEquals(OK, sqlite3_bind_int64(stmt, 1, 123456789012L))
                assertEquals(OK, sqlite3_bind_double(stmt, 2, 3.14))
                assertEquals(OK, sqlite3_bind_text(stmt, 3, "hello"))
                assertEquals(OK, sqlite3_bind_blob(stmt, 4, byteArrayOf(1, 2, 3, 4), 4, null))
                assertEquals(OK, sqlite3_bind_null(stmt, 5))
            },
        ) { stmt ->
            val intValue = assertNotNull(sqlite3_column_value(stmt, 0))
            assertEquals(INTEGER, sqlite3_value_type(intValue))
            assertEquals(INTEGER, sqlite3_value_numeric_type(intValue))
            assertEquals(123456789012L, sqlite3_value_int64(intValue))
            assertEquals(123456789012L.toInt(), sqlite3_value_int(intValue))
            assertEquals(1, sqlite3_value_frombind(intValue)) // came from a bound parameter

            val doubleValue = assertNotNull(sqlite3_column_value(stmt, 1))
            assertEquals(FLOAT, sqlite3_value_type(doubleValue))
            assertEquals(3.14, sqlite3_value_double(doubleValue))

            val textValue = assertNotNull(sqlite3_column_value(stmt, 2))
            assertEquals(TEXT, sqlite3_value_type(textValue))
            assertEquals("hello", sqlite3_value_text(textValue))
            assertEquals(5, sqlite3_value_bytes(textValue)) // "hello" is 5 bytes in UTF-8
            assertEquals(UTF8, sqlite3_value_encoding(textValue))

            val blobValue = assertNotNull(sqlite3_column_value(stmt, 3))
            assertEquals(BLOB, sqlite3_value_type(blobValue))
            assertContentEquals(byteArrayOf(1, 2, 3, 4), sqlite3_value_blob(blobValue))
            assertEquals(4, sqlite3_value_bytes(blobValue))

            val nullValue = assertNotNull(sqlite3_column_value(stmt, 4))
            assertEquals(NULL, sqlite3_value_type(nullValue))
            assertNull(sqlite3_value_text(nullValue))
        }
    }

    @Test
    fun valueFrombindIsFalseForComputedValues() = runSqliteConnectionTest { db ->
        // No bound parameters here: the value is computed by the engine itself, so
        // sqlite3_value_frombind should report false rather than true.
        stepOnce(db, "SELECT 1 + 1;") { stmt ->
            val computed = assertNotNull(sqlite3_column_value(stmt, 0))
            assertEquals(0, sqlite3_value_frombind(computed))
        }
    }

    @Test
    fun valueDupAndFreeWork() = runSqliteConnectionTest { db ->
        stepOnce(
            db,
            "SELECT ?;",
            bind = { assertEquals(OK, sqlite3_bind_text(it, 1, "duplicate me")) },
        ) { stmt ->
            val original = assertNotNull(sqlite3_column_value(stmt, 0))
            val duplicate = assertNotNull(sqlite3_value_dup(original))

            // The duplicate is an independent copy: same content, but freeing it must not
            // affect the original, which is still owned by the statement.
            assertEquals("duplicate me", sqlite3_value_text(duplicate))
            sqlite3_value_free(duplicate)
            assertEquals("duplicate me", sqlite3_value_text(original))
        }
    }

    private object PointerProbeMarker

    @Test
    fun valuePointerRoundTripsBoundPointer() = runSqliteConnectionTest { db ->
        var destroyCalled = false

        stepOnce(
            db,
            "SELECT ?;",
            bind = { stmt ->
                val bindResult = sqlite3_bind_pointer(
                    stmt = stmt,
                    index = 1,
                    data = PointerProbeMarker,
                    type = "probe",
                    destroy = { destroyCalled = true },
                )

                assertEquals(OK, bindResult)
            },
        ) { stmt ->
            val value = assertNotNull(sqlite3_column_value(stmt, 0))
            val roundTripped = sqlite3_value_pointer<PointerProbeMarker>(value, "probe")
            assertEquals(PointerProbeMarker, roundTripped)

            // A mismatched type tag must not hand back the pointer.
            assertNull(sqlite3_value_pointer<PointerProbeMarker>(value, "wrong-tag"))
        }

        // Destructor runs once the statement holding the binding is finalized
        assertTrue(destroyCalled)
    }

    @Test
    fun resultScalarSettersWork() = runSqliteConnectionTest { db ->
        val createResult = sqlite3_create_function_v2(
            db = db,
            name = "probe_scalar",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = Unit,
            step = null,
            final = null,
            func = { _, context, values ->
                when (assertNotNull(sqlite3_value_text(values[0]))) {
                    "int" -> sqlite3_result_int(context, 42)
                    "int64" -> sqlite3_result_int64(context, 4_200_000_000L)
                    "double" -> sqlite3_result_double(context, 2.5)
                    "text" -> sqlite3_result_text(context, "probed")
                    "null" -> sqlite3_result_null(context)
                    else -> error("Unexpected kind")
                }
            },
            destroy = null,
        )

        assertEquals(OK, createResult)

        stepOnce(
            db,
            "SELECT probe_scalar('int'), probe_scalar('int64'), probe_scalar('double'), " +
                    "probe_scalar('text'), probe_scalar('null');",
        ) { stmt ->
            assertEquals(42L, sqlite3_column_int64(stmt, 0))
            assertEquals(4_200_000_000L, sqlite3_column_int64(stmt, 1))
            assertEquals(2.5, sqlite3_column_double(stmt, 2))
            assertEquals("probed", sqlite3_column_text(stmt, 3))
            assertEquals(NULL, sqlite3_column_type(stmt, 4))
        }

        val deleteResult = sqlite3_create_function(
            db = db,
            name = "probe_scalar",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = null,
            func = null,
            step = null,
            final = null,
        )

        assertEquals(OK, deleteResult)
    }

    @Test
    fun resultBlobSettersWork() = runSqliteConnectionTest { db ->
        var blob64DestructorCalled = false
        var text64DestructorCalled = false

        val createResult = sqlite3_create_function_v2(
            db = db,
            name = "probe_blob",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = Unit,
            step = null,
            final = null,
            func = { _, context, values ->
                when (assertNotNull(sqlite3_value_text(values[0]))) {
                    "blob" -> sqlite3_result_blob(context, byteArrayOf(9, 8, 7), 3, null)

                    "blob64" -> {
                        val bytes = byteArrayOf(1, 2, 3, 4, 5)
                        val buffer = assertNotNull(sqlite3_malloc(bytes.size))
                        buffer.write(bytes)

                        sqlite3_result_blob64(context, buffer, bytes.size.toLong()) {
                            sqlite3_free(buffer)
                            blob64DestructorCalled = true
                        }
                    }

                    "text64" -> {
                        val bytes = "probed64".encodeToByteArray()
                        val buffer = assertNotNull(sqlite3_malloc(bytes.size))
                        buffer.write(bytes)

                        sqlite3_result_text64(context, buffer, bytes.size.toLong(), UTF8) {
                            sqlite3_free(buffer)
                            text64DestructorCalled = true
                        }
                    }

                    "zeroblob" -> sqlite3_result_zeroblob(context, 4)
                    "zeroblob64" -> assertEquals(OK, sqlite3_result_zeroblob64(context, 4uL))

                    else -> error("Unexpected kind")
                }
            },
            destroy = null,
        )

        assertEquals(OK, createResult)

        stepOnce(
            db,
            "SELECT probe_blob('blob'), probe_blob('blob64'), probe_blob('text64'), " +
                    "probe_blob('zeroblob'), probe_blob('zeroblob64');",
        ) { stmt ->
            assertContentEquals(byteArrayOf(9, 8, 7), sqlite3_column_blob(stmt, 0))
            assertContentEquals(byteArrayOf(1, 2, 3, 4, 5), sqlite3_column_blob(stmt, 1))
            assertEquals("probed64", sqlite3_column_text(stmt, 2))
            assertContentEquals(
                ByteArray(4),
                sqlite3_column_blob(stmt, 3)
            ) // zeroblob: 4 zero bytes
            assertContentEquals(ByteArray(4), sqlite3_column_blob(stmt, 4))
        }

        assertTrue(blob64DestructorCalled)
        assertTrue(text64DestructorCalled)

        val deleteResult = sqlite3_create_function(
            db = db,
            name = "probe_blob",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = null,
            func = null,
            step = null,
            final = null,
        )

        assertEquals(OK, deleteResult)
    }

    @Test
    fun resultValueAndSubtypeWork() = runSqliteConnectionTest { db ->
        // "tag_subtype" forwards its argument as the result (result_value) and stamps a subtype
        // on it; "read_subtype" reads that subtype back on whatever value it's given. Composing
        // them in one query is the only way to observe a subtype, since subtypes only travel
        // between application-defined functions within the same statement.
        val createTag = sqlite3_create_function_v2(
            db = db,
            name = "tag_subtype",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8 or RESULT_SUBTYPE,
            appData = Unit,
            step = null,
            final = null,
            func = { _, context, values ->
                sqlite3_result_value(context, values[0])
                sqlite3_result_subtype(context, 77u)
            },
            destroy = null,
        )

        assertEquals(OK, createTag)

        var observedSubtype: UInt? = null

        val createRead = sqlite3_create_function_v2(
            db = db,
            name = "read_subtype",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = Unit,
            step = null,
            final = null,
            func = { _, context, values ->
                observedSubtype = sqlite3_value_subtype(values[0])
                sqlite3_result_value(context, values[0])
            },
            destroy = null,
        )

        assertEquals(OK, createRead)

        stepOnce(db, "SELECT read_subtype(tag_subtype(123));") { stmt ->
            assertEquals(123L, sqlite3_column_int64(stmt, 0)) // result_value forwarded the payload
        }

        assertEquals(77u, observedSubtype)

        val deleteTagSubtypeResult = sqlite3_create_function(
            db = db,
            name = "tag_subtype",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = null,
            func = null,
            step = null,
            final = null
        )

        assertEquals(OK, deleteTagSubtypeResult)

        val deleteReadSubtypeResult = sqlite3_create_function(
            db = db,
            name = "read_subtype",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = null,
            func = null,
            step = null,
            final = null
        )

        assertEquals(OK, deleteReadSubtypeResult)
    }

    private data class PointerPayload(val label: String)

    @Test
    fun resultPointerRoundTripsBetweenFunctionsAndFreesOnStatementFinalize() =
        runSqliteConnectionTest { db ->
            var destroyCalled = false

            val createMake = sqlite3_create_function_v2(
                db = db,
                name = "make_pointer",
                nArg = 0,
                encoding = SqliteTextEncoding.UTF8,
                appData = Unit,
                step = null,
                final = null,
                func = { _, context, _ ->
                    sqlite3_result_pointer(context, PointerPayload("from-make"), "payload") {
                        destroyCalled = true
                    }
                },
                destroy = null,
            )

            assertEquals(OK, createMake)

            var observedPayload: PointerPayload? = null

            val createRead = sqlite3_create_function_v2(
                db = db,
                name = "read_pointer",
                nArg = 1,
                encoding = SqliteTextEncoding.UTF8,
                appData = Unit,
                step = null,
                final = null,
                func = { _, context, values ->
                    assertFails {
                        sqlite3_value_pointer<PointerProbeMarker>(values[0], "payload")
                    }

                    observedPayload = sqlite3_value_pointer<PointerPayload>(values[0], "payload")
                    sqlite3_result_null(context)
                },
                destroy = null,
            )

            assertEquals(OK, createRead)

            stepOnce(db, "SELECT read_pointer(make_pointer());") {
                // side effects checked below; the row's own value is irrelevant (NULL).
            }

            assertEquals(PointerPayload("from-make"), observedPayload)

            // pointer is scoped to the statement; freed once it's finalized
            assertTrue(destroyCalled)

            val deleteMakePointerResult = sqlite3_create_function(
                db = db,
                name = "make_pointer",
                nArg = 0,
                encoding = SqliteTextEncoding.UTF8,
                appData = null,
                func = null,
                step = null,
                final = null
            )

            assertEquals(OK, deleteMakePointerResult)

            val deleteReadPointerResult = sqlite3_create_function(
                db = db,
                name = "read_pointer",
                nArg = 1,
                encoding = SqliteTextEncoding.UTF8,
                appData = null,
                func = null,
                step = null,
                final = null
            )

            assertEquals(OK, deleteReadPointerResult)
        }

    @Test
    fun resultErrorSettersWork() = runSqliteConnectionTest { db ->
        val createResult = sqlite3_create_function_v2(
            db = db,
            name = "probe_error",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = Unit,
            step = null,
            final = null,
            func = { _, context, values ->
                when (assertNotNull(sqlite3_value_text(values[0]))) {
                    "message" -> sqlite3_result_error(context, "custom failure")
                    "code" -> sqlite3_result_error_code(context, CONSTRAINT)
                    "toobig" -> sqlite3_result_error_toobig(context)
                    "nomem" -> sqlite3_result_error_nomem(context)
                    else -> error("Unexpected kind")
                }
            },
            destroy = null,
        )

        assertEquals(OK, createResult)

        fun stepAndExpectFailure(sql: String): SqliteResultCode {
            val outStmt = sqlite3_stmt.OutputParam()
            assertEquals(OK, sqlite3_prepare_v2(db, sql, outStmt))

            val stmt = assertNotNull(outStmt.value)
            val stepResult = sqlite3_step(stmt)
            assertEquals(stepResult, sqlite3_finalize(stmt))

            return stepResult
        }

        assertEquals(ERROR, stepAndExpectFailure("SELECT probe_error('message');"))
        assertEquals(CONSTRAINT, stepAndExpectFailure("SELECT probe_error('code');"))
        assertEquals(TOOBIG, stepAndExpectFailure("SELECT probe_error('toobig');"))
        assertEquals(NOMEM, stepAndExpectFailure("SELECT probe_error('nomem');"))

        val deleteResult = sqlite3_create_function(
            db = db,
            name = "probe_error",
            nArg = 1,
            encoding = SqliteTextEncoding.UTF8,
            appData = null,
            func = null,
            step = null,
            final = null,
        )


        assertEquals(OK, deleteResult)
    }
}