package ksqlite.kapi.cipher

import ksqlite.capi.sqlite3mc_vfs_destroy
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.kapi.vfs.VirtualFileSystem

/**
 * SQLite Multiple Cipher specific [VirtualFileSystem] implementation.
 */
internal class CipherVirtualFileSystem(vfs: sqlite3_vfs) : VirtualFileSystem(vfs) {

    override fun onClose() {
        sqlite3mc_vfs_destroy(vfs.zName)
    }
}