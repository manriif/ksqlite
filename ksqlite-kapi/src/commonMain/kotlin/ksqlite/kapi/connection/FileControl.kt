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
 *
 * Every member requires an explicit `database`, `null` targeting the main database. See the
 * extension properties and functions below for shorthands that target the main database.
 */
public interface FileControl {

    /**
     * Returns the platform specific error code for [database], or `null` if the option is not
     * supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getSystemError(database: String?): Int?

    /**
     * Gives the VFS layer a hint of how large the database file for [database] will grow during
     * the current transaction. Does nothing if the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setSizeHint(
        size: Long,
        database: String?
    )

    /**
     * Sets the size of a chunk the VFS should split the database file for [database] into. Does
     * nothing if the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setChunkSize(
        size: Int,
        database: String?
    )

    /**
     * Returns whether the persistent WAL mode is enabled for [database], or `null` if the option
     * is not supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun isPersistWal(database: String?): Boolean?

    /**
     * Enables or disables the persistent WAL mode for [database]. Does nothing if the VFS does
     * not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setPersistWal(
        enabled: Boolean,
        database: String?
    )

    /**
     * Indicates that the entire database file for [database] will be overwritten by the current
     * transaction. Does nothing if the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setOverwrite(
        value: Long,
        database: String?
    )

    /**
     * Returns the names of all VFS shims and the final bottom-level VFS for [database], or `null`
     * if the option is not supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getVfsName(database: String?): String?

    /**
     * Returns whether the zero-damage mode is enabled for [database], or `null` if the option is
     * not supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun isPowerSafeOverwrite(database: String?): Boolean?

    /**
     * Enables or disables the zero-damage mode for [database]. Does nothing if the VFS does not
     * support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setPowerSafeOverwrite(
        enabled: Boolean,
        database: String?
    )

    /**
     * Returns a generated temporary file name for [database], or `null` if the option is not
     * supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getTempFileName(database: String?): String?

    /**
     * Returns the maximum number of bytes that will be used for memory-mapped I/O on [database],
     * or `null` if the option is not supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getMmapSize(database: String?): Long?

    /**
     * Sets the maximum number of bytes that will be used for memory-mapped I/O on [database].
     * Does nothing if the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setMmapSize(
        size: Long,
        database: String?
    )

    /**
     * Returns whether the file for [database] has been renamed, moved, or deleted since it was
     * first opened, or `null` if the option is not supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun hasMoved(database: String?): Boolean?

    /**
     * Sets the duration, in milliseconds, this connection waits to obtain a file lock on
     * [database] before failing. Does nothing if the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setLockTimeout(
        millis: Int,
        database: String?
    )

    /**
     * Returns a value for [database] that changes every time the database's content is modified,
     * whether from this connection or another one, usable to detect changes made outside this
     * connection.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getDataVersion(database: String?): Int

    /**
     * Returns the upper bound on the size of the in-memory database for [database], or `null` if
     * the option is not supported by the VFS.
     *
     * @throws ksqlite.kapi.SQLiteException if reading the option fails.
     */
    public fun getSizeLimit(database: String?): Long?

    /**
     * Sets the upper bound on the size of the in-memory database for [database]. Does nothing if
     * the VFS does not support the option.
     *
     * @throws ksqlite.kapi.SQLiteException if writing the option fails.
     */
    public fun setSizeLimit(
        limit: Long,
        database: String?
    )

    /**
     * Sets the number of bytes of reserved space at the end of each page of [database] to
     * [bytes], available to extensions such as an encryption codec for a nonce or checksum.
     *
     * @throws ksqlite.kapi.SQLiteException if the control request fails.
     */
    public fun reserveBytes(
        bytes: Int,
        database: String?
    )

    /**
     * Purges the content of the in-memory page cache for [database].
     *
     * @throws ksqlite.kapi.SQLiteException if the control request fails.
     */
    public fun resetCache(database: String?)
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Shorthand for [FileControl.getSystemError] on the main database.
 */
public val FileControl.systemError: Int?
    get() = getSystemError(null)

/**
 * Shorthand for [setSizeHint] on the main database.
 */
public fun FileControl.setSizeHint(size: Long): Unit = setSizeHint(size, null)

/**
 * Shorthand for [setChunkSize] on the main database.
 */
public fun FileControl.setChunkSize(size: Int): Unit = setChunkSize(size, null)

/**
 * Shorthand for [isPersistWal] and [FileControl.setPersistWal] on the main database.
 *
 * @throws IllegalArgumentException if set to `null`, there is no such thing as an unspecified
 * persistent WAL mode.
 */
public var FileControl.isPersistWal: Boolean?
    get() = isPersistWal(null)
    set(value) = setPersistWal(
        enabled = requireNotNull(value) { "Cannot set isPersistWal to null" },
        database = null
    )

/**
 * Shorthand for [setOverwrite] on the main database.
 */
public fun FileControl.setOverwrite(value: Long): Unit = setOverwrite(value, null)

/**
 * Shorthand for [FileControl.getVfsName] on the main database.
 */
public val FileControl.vfsName: String?
    get() = getVfsName(null)

/**
 * Shorthand for [isPowerSafeOverwrite] and [FileControl.setPowerSafeOverwrite] on the main
 * database.
 *
 * @throws IllegalArgumentException if set to `null`, there is no such thing as an unspecified
 * zero-damage mode.
 */
public var FileControl.isPowerSafeOverwrite: Boolean?
    get() = isPowerSafeOverwrite(null)
    set(value) = setPowerSafeOverwrite(
        enabled = requireNotNull(value) { "Cannot set isPowerSafeOverwrite to null" },
        database = null
    )

/**
 * Shorthand for [FileControl.getTempFileName] on the main database.
 */
public val FileControl.tempFileName: String?
    get() = getTempFileName(null)

/**
 * Shorthand for [FileControl.getMmapSize] and [FileControl.setMmapSize] on the main database.
 *
 * @throws IllegalArgumentException if set to `null`, there is no such thing as an unspecified
 * mmap size.
 */
public var FileControl.mmapSize: Long?
    get() = getMmapSize(null)
    set(value) = setMmapSize(
        size = requireNotNull(value) { "Cannot set mmapSize to null" },
        database = null
    )

/**
 * Shorthand for [hasMoved] on the main database.
 */
public val FileControl.hasMoved: Boolean?
    get() = hasMoved(null)

/**
 * Shorthand for [setLockTimeout] on the main database.
 */
public fun FileControl.setLockTimeout(millis: Int): Unit = setLockTimeout(millis, null)

/**
 * Shorthand for [FileControl.getDataVersion] on the main database.
 */
public val FileControl.dataVersion: Int
    get() = getDataVersion(null)

/**
 * Shorthand for [FileControl.getSizeLimit] and [FileControl.setSizeLimit] on the main database.
 *
 * @throws IllegalArgumentException if set to `null`, there is no such thing as an unspecified
 * size limit.
 */
public var FileControl.sizeLimit: Long?
    get() = getSizeLimit(null)
    set(value) = setSizeLimit(
        limit = requireNotNull(value) { "Cannot set sizeLimit to null" },
        database = null
    )

/**
 * Shorthand for [reserveBytes] on the main database.
 */
public fun FileControl.reserveBytes(bytes: Int): Unit = reserveBytes(bytes, null)

/**
 * Shorthand for [resetCache] on the main database.
 */
public fun FileControl.resetCache(): Unit = resetCache(null)
