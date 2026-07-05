package ksqlite.capi

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.vfs.SqliteVfsOpenFlagsOutputParam
import ksqlite.capi.vfs.sqlite3_file
import ksqlite.capi.vfs.xAccess
import ksqlite.capi.vfs.xClose
import ksqlite.capi.vfs.xDelete
import ksqlite.capi.vfs.xOpen
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
        val vfs = assertNotNull(sqlite3_vfs_find(null))

        assertTrue(vfs.iVersion >= VERSION_1)
        assertTrue(vfs.szOsFile > 0)
        assertTrue(vfs.mxPathname > 0)
        assertTrue(vfs.zName.isNotBlank())
    }

    @Test
    fun memberFunctionsWorks() = runSqliteTest {
        val vfs = assertNotNull(sqlite3_vfs_find(null))
        val file = sqlite3_file(vfs)
        val path = ksqliteTemporaryTestFile("vfs.db")

        val inOpenFlags = SqliteOpenFlag.READWRITE.vfs() or CREATE or DELETEONCLOSE or TEMP_DB
        val outOpenFlags = SqliteVfsOpenFlagsOutputParam()
        val openResult = vfs.xOpen(path, file, inOpenFlags, outOpenFlags)
        assertEquals(OK, openResult)

        val openFlags = outOpenFlags.value
        assertTrue(SqliteOpenFlag.READWRITE in openFlags)
        assertTrue(SqliteOpenFlag.CREATE in openFlags)
        assertTrue(SqliteOpenFlag.DELETEONCLOSE in openFlags)
        assertTrue(SqliteOpenFlag.TEMP_DB in openFlags)

        val outAccessExists = Int32OutputParam(-1)
        val accessExistsResult = vfs.xAccess(path, EXISTS, outAccessExists)
        assertEquals(OK, accessExistsResult)
        assertEquals(0, outAccessExists.value)

        val outAccessReadwrite = Int32OutputParam(-1)
        val accessReadwriteResult = vfs.xAccess(path, READWRITE, outAccessReadwrite)
        assertEquals(OK, accessReadwriteResult)
        assertEquals(0, outAccessReadwrite.value)

        val ioMethods = assertNotNull(file.pMethods)
        val closeResult = ioMethods.xClose(file)
        assertEquals(OK, closeResult)

        val deleteResult = vfs.xDelete(path, 0)
        assertEquals(SqliteResultCode.IOERR.DELETE_NOENT, deleteResult)
    }
}