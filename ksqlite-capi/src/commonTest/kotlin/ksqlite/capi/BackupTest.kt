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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests the Backup APIs.
 */
class BackupTest {
    
    @Test
    fun itWorks() = runSqliteTest {
        val outDb1 = sqlite3.OutputParam()
        val openDb1Result = sqlite3_open(":memory:", outDb1)
        assertEquals(OK, openDb1Result)
        val db1 = assertNotNull(outDb1.value)
        
        val outDb2 = sqlite3.OutputParam()
        val openDb2Result = sqlite3_open(":memory:", outDb2)
        assertEquals(OK, openDb2Result)
        val db2 = assertNotNull(outDb2.value)

        val insertSql = """
            CREATE TABLE fruits(name TEXT NOT NULL);
            INSERT INTO fruits VALUES ('Pêche');
        """.trimIndent()

        val insertResult = sqlite3_exec(db1, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        val backup = assertNotNull(sqlite3_backup_init(db2, "main", db1, "main"))

        val stepResult = sqlite3_backup_step(backup, -1)
        assertEquals(DONE, stepResult)

        val pageCount = sqlite3_backup_pagecount(backup)
        assertTrue(pageCount > 0)

        val remaining = sqlite3_backup_remaining(backup)
        assertEquals(remaining, 0)

        val finishResult = sqlite3_backup_finish(backup)
        assertEquals(OK, finishResult)

        // Verify
        val selectSql = "SELECT * FROM fruits;"
        var fruit: String? = null

        val selectResult = sqlite3_exec(db2, selectSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            assertNull(fruit)
            fruit = assertNotNull(values[0])
            0
        }

        assertEquals(OK, selectResult)
        assertEquals("Pêche", fruit)

        val closeDb1Result = sqlite3_close(db1)
        assertEquals(OK, closeDb1Result)

        val closeDb2Result = sqlite3_close(db2)
        assertEquals(OK, closeDb2Result)
    }
}