package ksqlite.kapi.config

import ksqlite.capi.types.SqliteConfigOption
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.helpers.usingBooleanParam

internal class ConfigurationScopeImpl :
    ConfigurationScope,
    AnyTimeConfigurationImpl() {

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

    override fun setPageCacheConfig(pMem: Buffer?, sz: Int, n: Int) =
        applyOption(SqliteConfigOption.PAGECACHE(pMem?.buffer, sz, n))

    override fun setHeapConfig(pMem: Buffer?, nBytes: Int, min: Int) =
        applyOption(SqliteConfigOption.HEAP(pMem?.buffer, nBytes, min))

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

    override fun setSorterReferenceSizeThreshold(nByte: Int) =
        applyOption(SqliteConfigOption.SORTERREF_SIZE(nByte))

    override fun setInMemoryDatabaseMaxSize(maxSize: Long) =
        applyOption(SqliteConfigOption.MEMDB_MAXSIZE(maxSize))
}