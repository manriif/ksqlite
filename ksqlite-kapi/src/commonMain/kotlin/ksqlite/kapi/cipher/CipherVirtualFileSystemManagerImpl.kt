package ksqlite.kapi.cipher

import ksqlite.capi.sqlite3_vfs_find
import ksqlite.capi.sqlite3mc_vfs_create
import ksqlite.capi.sqlite3mc_vfs_shutdown
import ksqlite.kapi.SQLiteException
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.vfs.VirtualFileSystem
import ksqlite.kapi.vfs.VirtualFileSystemBase

internal class CipherVirtualFileSystemManagerImpl(private val scope: CloseableScope) :
    CipherVirtualFileSystemManager {

    override fun create(
        vfs: VirtualFileSystemBase,
        makeDefault: Boolean
    ): VirtualFileSystem = scope.notClosed {
        sqliteResultCheck(sqlite3mc_vfs_create(vfs.zName, if (makeDefault) 1 else 0))

        runCatching {
            val newVfsName = sqliteMcVfsName(vfs.zName)

            sqlite3_vfs_find(newVfsName)
                ?.let(::CipherVirtualFileSystem)
                ?: throw CipherException("Vfs for name $newVfsName was not found")
        }.getOrElse { cause ->
            throw SQLiteException(
                result = ERROR,
                message = "An error occurred with the newly created VFS",
                cause = cause
            )
        }
    }

    override fun destroyAll() =
        scope.notClosed { sqlite3mc_vfs_shutdown() }
}