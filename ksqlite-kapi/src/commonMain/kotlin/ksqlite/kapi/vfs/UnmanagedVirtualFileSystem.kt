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
import ksqlite.types.vfs.SqliteVfs

/**
 * Wrapper around [sqlite3_vfs] for VFS that are managed by SQLite.
 */
internal class UnmanagedVirtualFileSystem(internal val vfs: sqlite3_vfs) :
    VirtualFileSystemBase,
    SqliteVfs by vfs {

    override val pNext: VirtualFileSystemBase?
        get() = vfs.pNext?.let(::UnmanagedVirtualFileSystem)
}