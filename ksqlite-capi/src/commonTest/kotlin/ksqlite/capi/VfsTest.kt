package ksqlite.capi

import ksqlite.capi.vfs.SqliteVfsAccessFlagsOutputParam
import ksqlite.capi.vfs.SqliteVfsOpenFlagsOutputParam
import ksqlite.capi.vfs.sqlite3_file
import ksqlite.capi.vfs.xAccess
import ksqlite.capi.vfs.xClose
import ksqlite.capi.vfs.xDelete
import ksqlite.capi.vfs.xOpen
import ksqlite.types.SqliteAccessFlag
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.vfs.SqliteVfsVersion
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

        assertTrue(vfs.iVersion >= SqliteVfsVersion.VERSION_1)
        assertTrue(vfs.szOsFile > 0)
        assertTrue(vfs.mxPathname > 0)
        assertTrue(vfs.zName.isNotBlank())
    }

    @Test
    fun memberFunctionsWorks() = runSqliteTest {
        val vfs = assertNotNull(sqlite3_vfs_find(null))
        val file = sqlite3_file(vfs)
        val path = ksqliteTemporaryTestFile("vfs.db")

        val inOpenFlags = SqliteOpenFlag.READWRITE.vfs() or
                SqliteOpenFlag.CREATE or
                SqliteOpenFlag.DELETEONCLOSE or
                SqliteOpenFlag.TEMP_DB

        val outOpenFlags = SqliteVfsOpenFlagsOutputParam()
        val openResult = vfs.xOpen(path, file, inOpenFlags, outOpenFlags)
        assertEquals(SqliteResultCode.OK, openResult)

        val openFlags = outOpenFlags.value
        assertTrue(SqliteOpenFlag.READWRITE in openFlags)
        assertTrue(SqliteOpenFlag.CREATE in openFlags)
        assertTrue(SqliteOpenFlag.DELETEONCLOSE in openFlags)
        assertTrue(SqliteOpenFlag.TEMP_DB in openFlags)

        val inAccessFlags = SqliteAccessFlag.EXISTS
        val outAccessFlags = SqliteVfsAccessFlagsOutputParam()
        val accessResult = vfs.xAccess(path, inAccessFlags, outAccessFlags)
        assertEquals(SqliteResultCode.OK, accessResult)

        val accessFlags = outAccessFlags.value
        assertTrue(SqliteAccessFlag.EXISTS in accessFlags)

        val ioMethods = assertNotNull(file.pMethods)
        val closeResult = ioMethods.xClose(file)
        assertEquals(SqliteResultCode.OK, closeResult)

        val deleteResult = vfs.xDelete(path, 0)
        assertEquals(SqliteResultCode.IOERR.DELETE_NOENT, deleteResult)
    }
}