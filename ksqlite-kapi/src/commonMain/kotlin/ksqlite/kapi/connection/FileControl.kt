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
package ksqlite.kapi.connection

/**
 * Exposes the [file control](https://sqlite.org/c3ref/file_control.html) APIs.
 */
public interface FileControl {

    /**
     * Returns the platform specific error code, on [database].
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getSystemError(database: String? = null): Int?

    /**
     * Gives the VFS layer a hint of how large the database file will grow to be during the current
     * transaction, on [database].
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setSizeHint(
        size: Long,
        database: String? = null
    )

    /**
     * Sets the size of a chunk the VFS should split the database file in.
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setChunkSize(
        size: Int,
        database: String? = null
    )

    /**
     * Returns whether the persistent WAL mode is enabled.
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun isPersistWal(database: String? = null): Boolean?

    /**
     * Enables or disables the persistent WAL mode.
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setPersistWal(
        enabled: Boolean,
        database: String? = null
    )

    /**
     * Indicates that the entire database file will be overwritten by the current transaction.
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setOverwrite(
        value: Long,
        database: String?
    )

    /**
     * Returns the names of all VFS shims and the final bottom-level VFS.
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getVfsName(database: String? = null): String?

    /**
     * Returns whether the zero-damage mode is enabled.
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun isPowerSafeOverwrite(database: String? = null): Boolean?

    /**
     * Enables or disables the zero-damage mode.
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setPowerSafeOverwrite(
        enabled: Boolean,
        database: String? = null
    )

    /**
     * Returns a generated temporary file name.
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getTempFileName(database: String? = null): String?

    /**
     * Returns the maximum number of bytes that will be used for memory-mapped I/O.
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getMmapSize(database: String? = null): Long?

    /**
     * Sets the maximum number of bytes that will be used for memory-mapped I/O.
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setMmapSize(
        size: Long,
        database: String? = null
    )

    /**
     * Returns whether the file has been renamed, moved, or deleted since it was first opened.
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun hasMoved(database: String? = null): Boolean?

    /**
     * Sets the duration, in milliseconds, before failing when attempting to obtain a file lock
     * using the xLock or xShmLock methods of the VFS.
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setLockTimeout(
        millis: Int,
        database: String? = null
    )

    /**
     * Returns the data version used to detect changes to a database file.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getDataVersion(database: String? = null): Int

    /**
     * Returns the upper bound on the size of the in-memory database, on [database].
     * If the option is not supported by the VFS, then `null` is returned.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getSizeLimit(database: String? = null): Long?

    /**
     * Sets the upper bound on the size of the in-memory database, on [database].
     * Does nothing is the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setSizeLimit(
        limit: Long,
        database: String? = null
    )

    /**
     * @throws ksqlite.kapi.SQLiteException if the control request fails.
     */
    public fun reserveBytes(
        bytes: Int,
        database: String?
    )

    /**
     * Purges the content of the in-memory page cache.
     *
     * @throws ksqlite.kapi.SQLiteException if the control request fails.
     */
    public fun resetCache(database: String? = null)
}