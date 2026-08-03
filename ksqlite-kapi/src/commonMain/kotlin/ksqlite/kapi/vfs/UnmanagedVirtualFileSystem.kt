package ksqlite.kapi.vfs

import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.types.vfs.SqliteVfs

/**
 * Wrapper around [sqlite3_vfs] for VFS that are managed by SQLite.
 */
internal class UnmanagedVirtualFileSystem(override val vfs: sqlite3_vfs) :
    VirtualFileSystemBase(),
    SqliteVfs by vfs {

    override val pNext: VirtualFileSystemBase?
        get() = vfs.pNext?.let(::UnmanagedVirtualFileSystem)
}