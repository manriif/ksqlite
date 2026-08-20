package ksqlite.kapi.cipher

import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.kapi.vfs.VirtualFileSystem

/**
 * SQLite Multiple Cipher specific [VirtualFileSystem] implementation.
 */
internal class CipherVirtualFileSystem(
    vfs: sqlite3_vfs,
    val name: String,
    var manager: CipherVirtualFileSystemManagerImpl?
) : VirtualFileSystem(vfs) {

    override fun onClose() {
        manager?.destroy(this)
    }
}