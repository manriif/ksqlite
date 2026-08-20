package ksqlite.kapi.cipher

import co.touchlab.stately.collections.ConcurrentMutableSet
import ksqlite.capi.sqlite3_vfs_find
import ksqlite.capi.sqlite3mc_vfs_create
import ksqlite.capi.sqlite3mc_vfs_destroy
import ksqlite.capi.sqlite3mc_vfs_shutdown
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.throwSQLiteException
import ksqlite.kapi.vfs.VirtualFileSystem
import ksqlite.kapi.vfs.VirtualFileSystemBase

internal class CipherVirtualFileSystemManagerImpl(private val scope: CloseableScope) :
    CipherVirtualFileSystemManager {

    private val cipherVirtualFileSystems = ConcurrentMutableSet<CipherVirtualFileSystem>()

    override fun create(
        vfs: VirtualFileSystemBase,
        makeDefault: Boolean
    ): VirtualFileSystem = scope.notClosed {
        sqliteResultCheck(sqlite3mc_vfs_create(vfs.zName, if (makeDefault) 1 else 0))

        val newVfsName = sqliteMcVfsName(vfs.zName)

        sqlite3_vfs_find(newVfsName)
            ?.let { CipherVirtualFileSystem(it, newVfsName, this) }
            ?.also(cipherVirtualFileSystems::add)
            ?: throwSQLiteException("Vfs for name $newVfsName was not found")
    }

    fun destroy(vfs: CipherVirtualFileSystem) {
        if (cipherVirtualFileSystems.remove(vfs)) {
            sqlite3mc_vfs_destroy(vfs.name)
        }
    }

    override fun destroyAll(): Unit = scope.notClosed {
        sqlite3mc_vfs_shutdown()

        cipherVirtualFileSystems.block { virtualFileSystems ->
            virtualFileSystems
                .onEach { vfs ->
                    vfs.manager = null
                    vfs.close()
                }
                .clear()
        }
    }
}