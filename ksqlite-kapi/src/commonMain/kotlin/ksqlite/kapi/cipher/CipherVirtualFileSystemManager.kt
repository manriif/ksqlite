package ksqlite.kapi.cipher

import ksqlite.kapi.vfs.VirtualFileSystem
import ksqlite.kapi.vfs.VirtualFileSystemBase

/**
 * Manages the SQLite Multiple Ciphers virtual file systems.
 */
public interface CipherVirtualFileSystemManager {

    /**
     * Creates and returns a [VirtualFileSystemBase] that wraps [vfs].
     *
     * If [makeDefault] is `true`, which is the default behavior, then the newly created virtual
     * file system is set as the default one.
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurred while creating the virtual file
     * system.
     */
    public fun create(
        vfs: VirtualFileSystemBase,
        makeDefault: Boolean = true
    ): VirtualFileSystem

    /**
     * Destroys all the registered SQLite3 Multiple Ciphers virtual file systems.
     */
    public fun destroyAll()
}