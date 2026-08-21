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
package ksqlite.kapi.cipher

import ksqlite.kapi.vfs.VirtualFileSystem
import ksqlite.kapi.vfs.VirtualFileSystemBase

/**
 * Creates and destroys the virtual file systems SQLite Multiple Ciphers wraps around an existing
 * one to transparently encrypt and decrypt the files it manages.
 */
public interface CipherVirtualFileSystemManager {

    /**
     * Creates and returns a virtual file system wrapping [vfs] for encryption.
     *
     * [makeDefault] sets it as the default virtual file system used when none is explicitly
     * requested.
     *
     * @throws ksqlite.kapi.SQLiteException if creating it fails.
     */
    public fun create(
        vfs: VirtualFileSystemBase,
        makeDefault: Boolean = true
    ): VirtualFileSystem

    /**
     * Destroys every cipher virtual file system created through this manager. Every
     * [VirtualFileSystem] previously returned by [create] becomes closed as a result, even ones
     * still in use.
     */
    public fun destroyAll()
}