package ksqlite.kapi.vfs

import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.internal.runtime.closeable.DelegatingCloseableScope
import ksqlite.types.vfs.SqliteVfsVersion

/**
 * Represents a [virtual file system](https://sqlite.org/c3ref/vfs.html).
 */
public abstract class VirtualFileSystem internal constructor(internal val vfs: sqlite3_vfs) :
    VirtualFileSystemBase,
    AutoCloseable {

    private val scope = DelegatingCloseableScope(::onClose)

    override val iVersion: SqliteVfsVersion
        get() = scope.notClosed { vfs.iVersion }

    override val szOsFile: Int
        get() = scope.notClosed { vfs.szOsFile }

    override val mxPathname: Int
        get() = scope.notClosed { vfs.mxPathname }

    override val zName: String
        get() = scope.notClosed { vfs.zName }

    override val pNext: VirtualFileSystemBase?
        get() = scope.notClosed { vfs.pNext?.let(::UnmanagedVirtualFileSystem) }

    /**
     * Releases this virtual file system's native resources.
     */
    protected abstract fun onClose()

    /**
     * Destroys this virtual file system.
     */
    final override fun close(): Unit = scope.close()
}