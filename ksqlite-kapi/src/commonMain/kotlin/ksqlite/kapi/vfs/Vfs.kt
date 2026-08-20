package ksqlite.kapi.vfs

import ksqlite.capi.vfs.sqlite3_vfs

internal val VirtualFileSystemBase.vfs: sqlite3_vfs
    get() = when (this) {
        is UnmanagedVirtualFileSystem -> this.vfs
        is VirtualFileSystem -> this.vfs
    }