package ksqlite.kapi.vfs

import ksqlite.capi.sqlite3_vfs_find
import ksqlite.capi.sqlite3_vfs_register
import ksqlite.capi.sqlite3_vfs_unregister
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck

internal class VirtualFileSystemManagerImpl(private val scope: CloseableScope) :
    VirtualFileSystemManager {

    override val default: VirtualFileSystemBase?
        get() = findVfs(null)

    private fun findVfs(name: String?): VirtualFileSystemBase? = scope.notClosed {
        sqlite3_vfs_find(name)?.let(::UnmanagedVirtualFileSystem)
    }

    override fun find(name: String): VirtualFileSystemBase? = findVfs(name)

    override fun register(vfs: VirtualFileSystemBase, makeDefault: Boolean) = scope.notClosed {
        sqliteResultCheck(sqlite3_vfs_register(vfs.vfs, if (makeDefault) 1 else 0))
    }

    override fun unregister(vfs: VirtualFileSystemBase) = scope.notClosed {
        sqliteResultCheck(sqlite3_vfs_unregister(vfs.vfs))
    }
}