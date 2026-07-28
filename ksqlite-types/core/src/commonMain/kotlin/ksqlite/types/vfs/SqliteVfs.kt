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
package ksqlite.types.vfs

/**
 * Describes an [`sqlite3_vfs`](https://sqlite.org/c3ref/vfs.html) struct.
 */
public interface SqliteVfs {

    /**
     * Structure version number.
     */
    public val iVersion: SqliteVfsVersion

    /**
     * Size of subclassed sqlite3_file.
     */
    public val szOsFile: Int

    /**
     * Maximum file pathname length.
     */
    public val mxPathname: Int

    /**
     * Next registered VFS.
     */
    public val pNext: SqliteVfs?

    /**
     * Name of this virtual file system.
     */
    public val zName: String
}