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
package ksqlite.capi.vfs

import ksqlite.capi.findVfs
import ksqlite.capi.ksqliteTempTestFile
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.runSqliteTest
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests the Virtual File System API.
 */
class VfsTest {

    @Test
    fun memberPropertiesWorks() = runSqliteTest {
        val vfs = findVfs()

        println(vfs.zName)
        assertTrue(vfs.iVersion >= VERSION_1)
        assertTrue(vfs.szOsFile > 0)
        assertTrue(vfs.mxPathname > 0)
        assertTrue(vfs.zName.isNotBlank())
    }

    @Test
    fun memberFunctionsWorks() = runSqliteTest {
        val vfs = findVfs()
        val file = sqlite3_file(vfs)
        val path = ksqliteTempTestFile("vfs.db")

        val inOpenFlags = SqliteOpenFlag.READWRITE.vfs() or CREATE or DELETEONCLOSE or TEMP_DB
        val outOpenFlags = SqliteVfsOpenFlagsOutputParam()
        val openResult = vfs.xOpen(path, file, inOpenFlags, outOpenFlags)
        assertEquals(OK, openResult)

        val openFlags = outOpenFlags.value
        assertTrue(SqliteOpenFlag.READWRITE in openFlags)

        val outAccessExists = Int32OutputParam(-1)
        val accessExistsResult = vfs.xAccess(path, EXISTS, outAccessExists)
        assertEquals(OK, accessExistsResult)
        assertEquals(0, outAccessExists.value)

        val outAccessReadwrite = Int32OutputParam(-1)
        val accessReadwriteResult = vfs.xAccess(path, READWRITE, outAccessReadwrite)
        assertEquals(OK, accessReadwriteResult)

        val ioMethods = assertNotNull(file.pMethods)
        val closeResult = ioMethods.xClose(file)
        assertEquals(OK, closeResult)

        val deleteResult = vfs.xDelete(path, 0)
        assertEquals(SqliteResultCode.IOERR.DELETE_NOENT, deleteResult)

        file.close()
    }
}