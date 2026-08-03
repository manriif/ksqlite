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
package ksqlite.kapi.config

import ksqlite.capi.types.SqliteConfigOption
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.usingBooleanParam

internal class ConfigurationScopeImpl(scope: ClosableScope) :
    ConfigurationScope,
    AnyTimeConfigurationImpl(scope) {

    override var isRowidInViewActivated: Boolean
        get() = usingBooleanParam(null) { applyOption(SqliteConfigOption.ROWID_IN_VIEW(it)) }
        set(value) {
            val _ = usingBooleanParam(value) { applyOption(SqliteConfigOption.ROWID_IN_VIEW(it)) }
        }

    override fun setSingleThread() =
        applyOption(SqliteConfigOption.SINGLETHREAD)

    override fun setMultiThread() =
        applyOption(SqliteConfigOption.MULTITHREAD)

    override fun setSerialized() =
        applyOption(SqliteConfigOption.SERIALIZED)

    override fun setMemStatusEnabled(enabled: Boolean) =
        applyOption(SqliteConfigOption.MEMSTATUS(if (enabled) 1 else 0))

    override fun setLookasideConfig(sz: Int, cnt: Int) =
        applyOption(SqliteConfigOption.LOOKASIDE(sz, cnt))

    override fun setUriEnabled(enabled: Boolean) =
        applyOption(SqliteConfigOption.URI(if (enabled) 1 else 0))

    override fun setCoveringIndexScanEnabled(enabled: Boolean) =
        applyOption(SqliteConfigOption.COVERING_INDEX_SCAN(if (enabled) 1 else 0))

    override fun setSqlLogger(logger: SqlLogger?) = logger
        ?.let { applyOption(SqliteConfigOption.SQLLOG(it, SqlLoggerCallback)) }
        ?: applyOption(SqliteConfigOption.SQLLOG(null, null))

    override fun setMmapSize(sz: Long, mx: Long) =
        applyOption(SqliteConfigOption.MMAP_SIZE(sz, mx))

    override fun setPackedMemoryArraySize(szPma: UInt) =
        applyOption(SqliteConfigOption.PMASZ(szPma))

    override fun setStatementJournalSpillThreshold(nByte: Int) =
        applyOption(SqliteConfigOption.STMTJRNL_SPILL(nByte))

    override fun setSmallMallocEnabled(enabled: Boolean) =
        applyOption(SqliteConfigOption.SMALL_MALLOC(if (enabled) 1 else 0))

    override fun setInMemoryDatabaseMaxSize(maxSize: Long) =
        applyOption(SqliteConfigOption.MEMDB_MAXSIZE(maxSize))
}