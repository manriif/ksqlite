package ksqlite.kapi.vfs

import ksqlite.types.vfs.SqliteVfs

/**
 * Base for all virtual file system implementations.
 */
public sealed interface VirtualFileSystemBase : SqliteVfs {

    /**
     * Next [VirtualFileSystemBase].
     */
    abstract override val pNext: VirtualFileSystemBase?
}