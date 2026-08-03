package ksqlite.kapi.vfs

import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.types.vfs.SqliteVfs

/**
 * Base for all virtual file system implementations.
 */
public abstract class VirtualFileSystemBase internal constructor(): SqliteVfs {

    /**
     * Virtual file system handle.
     */
    internal abstract val vfs: sqlite3_vfs

    /**
     * Next [VirtualFileSystemBase].
     */
    abstract override val pNext: VirtualFileSystemBase?
}