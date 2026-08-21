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
package ksqlite.kapi.vfs

import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.kapi.runSqliteWalFileTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests [FileName].
 */
class FileNameTest {

    @Test
    fun fileNameApiWorks() = runSqliteWalFileTest("kapi-filename.db") { sqlite, path ->
        val uri = "file:$path?mode=rwc&testFlag=1&testSize=42&testText=world&hello="
        val connection = sqlite.open(uri)

        val fileName = assertNotNull(connection.getFileName())
        // The path SQLite resolves may differ from the raw [path] (e.g. macOS resolves
        // /var/folders/... to /private/var/folders/...), so only the derived names are compared
        // against each other, and the resolved path is checked to at least refer to our file.
        val resolvedPath = assertNotNull(fileName.databaseFileName)
        assertTrue(resolvedPath.endsWith("kapi-filename.db"))
        assertEquals("$resolvedPath-journal", fileName.journalFileName)
        assertEquals("$resolvedPath-wal", fileName.walFileName)
        assertTrue(fileName.content.isNotEmpty())

        // Index 0 is "mode", added by SQLite itself from the `mode=rwc` URI parameter.
        assertEquals("mode", fileName.getKey(0))
        assertEquals("testFlag", fileName.getKey(1))
        assertEquals("hello", fileName.getKey(4))
        assertNull(fileName.getKey(99))

        assertEquals("world", fileName.getValue("testText"))
        assertNull(fileName.getValue("does-not-exist"))

        assertTrue(fileName.getValue("testFlag", default = false))
        assertEquals(true, fileName.getValue("unknownFlag", default = true))

        assertEquals(42L, fileName.getValue("testSize", default = 0L))
        assertEquals(7L, fileName.getValue("unknownSize", default = 7L))

        assertEquals(
            listOf("mode", "testFlag", "testSize", "testText", "hello"),
            fileName.keys()
        )

        connection.close()
    }

    @Test
    fun getFileNameForMemoryDatabaseIsEmpty() = runSqliteConnectionTest { _, connection ->
        // An in-memory database has an associated FileName, but an empty one.
        val fileName = assertNotNull(connection.getFileName())
        assertEquals("", fileName.content)
        assertTrue(fileName.databaseFileName.isNullOrEmpty())
    }
}
