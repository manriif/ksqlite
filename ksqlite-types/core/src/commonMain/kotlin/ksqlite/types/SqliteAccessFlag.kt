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
package ksqlite.types

/**
 * These integer constants can be used as the third parameter to the xAccess method of an
 * sqlite3_vfs object. They determine what kind of permissions the xAccess method is looking for.
 * With SQLITE_ACCESS_EXISTS, the xAccess method simply checks whether the file exists.
 *
 * [Flags for the xAccess VFS method](https://sqlite.org/c3ref/c_access_exists.html).
 */
public enum class SqliteAccessFlag(public open val value: Int) {

    /**
     * With SQLITE_ACCESS_EXISTS, the xAccess method simply checks whether the file exists.
     */
    EXISTS(0),

    /**
     * The SQLITE_ACCESS_READWRITE constant is currently used only by the temp_store_directory
     * pragma, though this could change in a future release of SQLite
     */
    READWRITE(1),

    /**
     * With SQLITE_ACCESS_READ, the xAccess method checks whether the file is readable. The
     * SQLITE_ACCESS_READ constant is currently unused, though it might be used in a future release
     * of SQLite.
     */
    READ(2)
}