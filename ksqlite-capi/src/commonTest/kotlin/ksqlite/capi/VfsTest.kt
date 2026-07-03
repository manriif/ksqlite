package ksqlite.capi

import ksqlite.capi.vfs.sqlite3_file
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
    fun fieldsWorks() = runSqliteTest {
        val vfs = assertNotNull(sqlite3_vfs_find(null))

        assertTrue(vfs.iVersion >= SqliteVfsVersion.VERSION_1)
        assertTrue(vfs.szOsFile > 0)
        assertTrue(vfs.mxPathname > 0)
        assertTrue(vfs.zName.isNotBlank())
    }

    @Test
    fun openWorks() {
        val vfs = assertNotNull(sqlite3_vfs_find(null))
        val file = sqlite3_file(vfs)

        val openFlags = SqliteOpenFlag.READWRITE.vfs() or
                SqliteOpenFlag.CREATE or
                SqliteOpenFlag.DELETEONCLOSE or
                SqliteOpenFlag.TEMP_DB

        val openResult = vfs.xOpen.apply(vfs, null, file, openFlags, null)
        assertEquals(SqliteResultCode.OK, openResult)

        val ioMethods = assertNotNull(file.pMethods)

        val closeResult = ioMethods.xClose.apply(file)
        assertEquals(SqliteResultCode.OK, closeResult)
    }
}