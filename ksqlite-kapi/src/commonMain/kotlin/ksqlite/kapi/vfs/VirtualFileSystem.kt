/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.kapi.vfs

import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.internal.runtime.closeable.DelegatingCloseableScope
import ksqlite.types.vfs.SqliteVfsVersion

/**
 * A user-implemented [virtual file system](https://sqlite.org/c3ref/vfs.html), registered
 * through [VirtualFileSystemManager.register].
 *
 * Unless documented otherwise, every member throws [IllegalStateException] once this virtual
 * file system is closed.
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
     * Destroys this virtual file system. Calling this again on an already closed instance has
     * no effect.
     */
    final override fun close(): Unit = scope.close()
}