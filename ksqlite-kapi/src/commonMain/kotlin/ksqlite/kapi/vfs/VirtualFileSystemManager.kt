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

/**
 * Manages the [VirtualFileSystemBase]s.
 */
public interface VirtualFileSystemManager {

    /**
     * Returns the default virtual file system.
     */
    public val default: VirtualFileSystemBase?

    /**
     * Finds the [VirtualFileSystemBase] registered under [name], or `null` if none matches.
     */
    public fun find(name: String): VirtualFileSystemBase?

    /**
     * Registers the given [vfs]. If [makeDefault] is `true`, it is set as the default one.
     *
     * @throws ksqlite.kapi.SQLiteException if registering the virtual file system fails.
     */
    public fun register(
        vfs: VirtualFileSystemBase,
        makeDefault: Boolean
    )

    /**
     * Unregisters the given [vfs].
     *
     * @throws ksqlite.kapi.SQLiteException if unregistering the virtual file system fails.
     */
    public fun unregister(vfs: VirtualFileSystemBase)
}