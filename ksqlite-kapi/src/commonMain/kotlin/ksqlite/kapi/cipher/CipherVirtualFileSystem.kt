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