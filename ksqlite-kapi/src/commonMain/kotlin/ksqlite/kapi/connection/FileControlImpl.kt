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

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_file_control
import ksqlite.capi.types.SqliteFileControlOpcode
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingBooleanParam
import ksqlite.kapi.helpers.usingParam
import ksqlite.types.SqliteResultCode

internal class FileControlImpl(
    private val db: sqlite3,
    private val scope: CloseableScope
) : FileControl {

    /**
     * Checks the [result] and returns whether the operation code was not found.
     */
    private fun isNotFound(result: SqliteResultCode): Boolean {
        if (result == SqliteResultCode.NOTFOUND) {
            return true
        }

        sqliteResultCheck(result)
        return false
    }

    ///////////////////////////////////////////////////////////////////////////
    // Boolean
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value of the option supplied by [createOption].
     */
    private inline fun getBooleanOption(
        database: String?,
        createOption: (Int32OutputParam) -> SqliteFileControlOpcode.IntParam,
        initialValue: Boolean? = null
    ): Boolean? = scope.notClosed {
        usingBooleanParam(initialValue) { param ->
            if (isNotFound(sqlite3_file_control(db, database, createOption(param)))) {
                return null
            }
        }
    }

    /**
     * Sets the [value] of the option supplied by [createOption].
     */
    private inline fun setBooleanOption(
        value: Boolean,
        database: String?,
        createOption: (Int32OutputParam) -> SqliteFileControlOpcode.IntParam
    ): Unit = scope.notClosed {
        val _ = usingParam(Int32OutputParam(if (value) 1 else 0)) { param ->
            if (isNotFound(sqlite3_file_control(db, database, createOption(param)))) {
                return
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Int
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value of the option supplied by [createOption].
     */
    private inline fun getIntOption(
        database: String?,
        createOption: (Int32OutputParam) -> SqliteFileControlOpcode.IntParam,
        nonAlteringValue: Int = -1
    ): Int? = scope.notClosed {
        usingParam(Int32OutputParam(nonAlteringValue)) { param ->
            if (isNotFound(sqlite3_file_control(db, database, createOption(param)))) {
                return null
            }
        }
    }

    /**
     * Sets the [value] of the option supplied by [createOption].
     */
    private inline fun setIntOption(
        value: Int,
        database: String?,
        createOption: (Int32OutputParam) -> SqliteFileControlOpcode.IntParam
    ): Unit = scope.notClosed {
        val _ = usingParam(Int32OutputParam(value)) { param ->
            if (isNotFound(sqlite3_file_control(db, database, createOption(param)))) {
                return
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Long
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value of the option supplied by [createOption].
     */
    private inline fun getLongOption(
        database: String?,
        createOption: (Int64OutputParam) -> SqliteFileControlOpcode.LongParam,
        nonAlteringValue: Long = -1
    ): Long? = scope.notClosed {
        usingParam(Int64OutputParam(nonAlteringValue)) { param ->
            if (isNotFound(sqlite3_file_control(db, database, createOption(param)))) {
                return null
            }
        }
    }

    /**
     * Sets the [value] of the option supplied by [createOption].
     */
    private inline fun setLongOption(
        value: Long,
        database: String?,
        createOption: (Int64OutputParam) -> SqliteFileControlOpcode.LongParam
    ): Unit = scope.notClosed {
        val _ = usingParam(Int64OutputParam(value)) { param ->
            if (isNotFound(sqlite3_file_control(db, database, createOption(param)))) {
                return
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // String
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value of the option supplied by [createOption].
     */
    private inline fun getStringOption(
        database: String?,
        createOption: (Utf8OutputParam) -> SqliteFileControlOpcode.StringParam
    ): String? = scope.notClosed {
        usingParam(Utf8OutputParam()) { param ->
            if (isNotFound(sqlite3_file_control(db, database, createOption(param)))) {
                return null
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Options
    ///////////////////////////////////////////////////////////////////////////

    override fun getSystemError(database: String?): Int? =
        getIntOption(database, SqliteFileControlOpcode::LAST_ERRNO)

    override fun setSizeHint(size: Long, database: String?) =
        setLongOption(size, database, SqliteFileControlOpcode::SIZE_HINT)

    override fun setChunkSize(size: Int, database: String?) =
        setIntOption(size, database, SqliteFileControlOpcode::CHUNK_SIZE)

    override fun isPersistWal(database: String?): Boolean? =
        getBooleanOption(database, SqliteFileControlOpcode::PERSIST_WAL)

    override fun setPersistWal(enabled: Boolean, database: String?) =
        setBooleanOption(enabled, database, SqliteFileControlOpcode::PERSIST_WAL)

    override fun setOverwrite(value: Long, database: String?) =
        setLongOption(value, database, SqliteFileControlOpcode::OVERWRITE)

    override fun getVfsName(database: String?): String? =
        getStringOption(database, SqliteFileControlOpcode::VFSNAME)

    override fun isPowerSafeOverwrite(database: String?): Boolean? =
        getBooleanOption(database, SqliteFileControlOpcode::POWERSAFE_OVERWRITE)

    override fun setPowerSafeOverwrite(enabled: Boolean, database: String?) =
        setBooleanOption(enabled, database, SqliteFileControlOpcode::POWERSAFE_OVERWRITE)

    override fun getTempFileName(database: String?): String? =
        getStringOption(database, SqliteFileControlOpcode::TEMPFILENAME)

    override fun getMmapSize(database: String?): Long? =
        getLongOption(database, SqliteFileControlOpcode::MMAP_SIZE)

    override fun setMmapSize(size: Long, database: String?) =
        setLongOption(size, database, SqliteFileControlOpcode::MMAP_SIZE)

    override fun hasMoved(database: String?): Boolean? =
        getBooleanOption(database, SqliteFileControlOpcode::HAS_MOVED)

    override fun setLockTimeout(millis: Int, database: String?) =
        setIntOption(millis, database, SqliteFileControlOpcode::LOCK_TIMEOUT)

    override fun getDataVersion(database: String?): Int =
        checkNotNull(getIntOption(database, SqliteFileControlOpcode::DATA_VERSION))

    override fun getSizeLimit(database: String?): Long? =
        getLongOption(database, SqliteFileControlOpcode::SIZE_LIMIT)

    override fun setSizeLimit(limit: Long, database: String?): Unit =
        setLongOption(limit, database, SqliteFileControlOpcode::SIZE_LIMIT)

    override fun reserveBytes(bytes: Int, database: String?) =
        setIntOption(bytes, database, SqliteFileControlOpcode::RESERVE_BYTES)

    override fun resetCache(database: String?): Unit = scope.notClosed {
        val _ = isNotFound(sqlite3_file_control(db, database, SqliteFileControlOpcode.RESET_CACHE))
    }
}