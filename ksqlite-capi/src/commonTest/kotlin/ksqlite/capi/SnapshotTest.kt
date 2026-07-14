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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the Snapshot APIs.
 */
class SnapshotTest {

    @Test
    fun comparisonWorks() = runSqliteWalFileTest("snapshot_compare.db") { path ->
        val outDb = sqlite3.OutputParam()
        val dbOpenResult = sqlite3_open(path, outDb)
        assertEquals(OK, dbOpenResult)

        val db = assertNotNull(outDb.value)

        val insert1Sql = """
            PRAGMA journal_mode=WAL;
            CREATE TABLE test(id INTEGER);
            INSERT INTO test VALUES (1);
        """.trimIndent()

        val insert1Result = sqlite3_exec(db, insert1Sql, null, null, null)
        assertEquals(OK, insert1Result)

        val beginSql = "BEGIN;"
        val commitSql = "COMMIT;"

        val begin1Result = sqlite3_exec(db, beginSql, null, null, null)
        assertEquals(OK, begin1Result)

        val outSnapshot1 = sqlite3_snapshot.OutputParam()
        val getSnapshot1Result = sqlite3_snapshot_get(db, "main", outSnapshot1)
        assertEquals(OK, getSnapshot1Result)

        val snapshot1 = assertNotNull(outSnapshot1.value)
        val snapshot1SelfCompare = sqlite3_snapshot_cmp(snapshot1, snapshot1)
        assertEquals(0, snapshot1SelfCompare)

        val commit1Result = sqlite3_exec(db, commitSql, null, null, null)
        assertEquals(OK, commit1Result)

        val insert2Sql = "INSERT INTO test VALUES (2);"
        val insert2Result = sqlite3_exec(db, insert2Sql, null, null, null)
        assertEquals(OK, insert2Result)

        val begin2Result = sqlite3_exec(db, beginSql, null, null, null)
        assertEquals(OK, begin2Result)

        val outSnapshot2 = sqlite3_snapshot.OutputParam()
        val getSnapshot2Result = sqlite3_snapshot_get(db, "main", outSnapshot2)
        assertEquals(OK, getSnapshot2Result)

        val snapshot2 = assertNotNull(outSnapshot2.value)
        val snapshot2SelfCompare = sqlite3_snapshot_cmp(snapshot1, snapshot1)
        assertEquals(0, snapshot2SelfCompare)

        val commit2Result = sqlite3_exec(db, commitSql, null, null, null)
        assertEquals(OK, commit2Result)

        val snapshotsCompare1 = sqlite3_snapshot_cmp(snapshot1, snapshot2)
        assertTrue(snapshotsCompare1 < 0)

        val snapshotsCompare2 = sqlite3_snapshot_cmp(snapshot2, snapshot1)
        assertTrue(snapshotsCompare2 > 0)

        sqlite3_snapshot_free(snapshot1)
        sqlite3_snapshot_free(snapshot2)

        val dbCloseResult = sqlite3_close(db)
        assertEquals(OK, dbCloseResult)
    }

    @Test
    fun openWorks() = runSqliteWalFileTest("snapshot_open.db") { path ->
        val pragmaSql = "PRAGMA journal_mode=WAL;"

        val outDb1 = sqlite3.OutputParam()
        val db1OpenResult = sqlite3_open(path, outDb1)
        assertEquals(OK, db1OpenResult)

        val db1 = assertNotNull(outDb1.value)
        val db1PragmaResult = sqlite3_exec(db1, pragmaSql, null, null, null)
        assertEquals(OK, db1PragmaResult)
        
        val outDb2 = sqlite3.OutputParam()
        val db2OpenResult = sqlite3_open(path, outDb2)
        assertEquals(OK, db2OpenResult)

        val db2 = assertNotNull(outDb2.value)
        val db2PragmaResult = sqlite3_exec(db2, pragmaSql, null, null, null)
        assertEquals(OK, db2PragmaResult)

        val beginSql = "BEGIN;"
        val commitSql = "COMMIT;"

        val insert1Sql = """
            CREATE TABLE test(id INTEGER);
            INSERT INTO test VALUES (1);
        """.trimIndent()

        val begin1Result = sqlite3_exec(db1, insert1Sql + beginSql, null, null, null)
        assertEquals(OK, begin1Result)

        val outSnapshot = sqlite3_snapshot.OutputParam()
        val getSnapshotResult = sqlite3_snapshot_get(db1, "main", outSnapshot)
        assertEquals(OK, getSnapshotResult)
        val snapshot = assertNotNull(outSnapshot.value)

        val commit1Result = sqlite3_exec(db1, commitSql, null, null, null)
        assertEquals(OK, commit1Result)

        val insert2Sql = "INSERT INTO test VALUES (2), (3);"
        val insert2Result = sqlite3_exec(db2, insert2Sql, null, null, null)
        assertEquals(OK, insert2Result)

        val begin2Result = sqlite3_exec(db1, beginSql, null, null, null)
        assertEquals(OK, begin2Result)

        val selectSql = "SELECT * FROM test;"
        val actualValues1 = mutableListOf<String>()

        val select1Result = sqlite3_exec(db1, selectSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            actualValues1.add(assertNotNull(values[0]))
            0
        }

        assertEquals(OK, select1Result)

        val expectedValues1 = listOf("1", "2", "3")
        assertContentEquals(expectedValues1, actualValues1)

        val snapshotOpenResult = sqlite3_snapshot_open(db1, "main", snapshot)
        assertEquals(OK, snapshotOpenResult)

        val actualValues2 = mutableListOf<String>()

        val select2Result = sqlite3_exec(db1, selectSql, null, null) { _, count, values, _ ->
            assertEquals(1, count)
            actualValues2.add(assertNotNull(values[0]))
            0
        }

        assertEquals(OK, select2Result)

        val expectedValues2 = listOf("1")
        assertContentEquals(expectedValues2, actualValues2)

        val commit2Result = sqlite3_exec(db1, commitSql, null, null, null)
        assertEquals(OK, commit2Result)

        val recoverResult = sqlite3_snapshot_recover(db2, "main")
        assertEquals(OK, recoverResult)

        sqlite3_snapshot_free(snapshot)

        val db1CloseResult = sqlite3_close(db1)
        assertEquals(OK, db1CloseResult)

        val db2CloseResult = sqlite3_close(db2)
        assertEquals(OK, db2CloseResult)
    }
}