package ksqlite.kapi.config

import ksqlite.capi.sqlite3_config
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingBooleanParam
import ksqlite.kapi.helpers.usingParam

internal class ConfigurationScopeImpl :
    ConfigurationScope,
    ClosableScope() {

    /**
     * Applies the given configuration [option].
     */
    private fun applyOption(option: SqliteConfigOption) =
        notClosed { sqliteResultCheck(sqlite3_config(option)) }

    override fun singlethread() =
        applyOption(SqliteConfigOption.SINGLETHREAD)

    override fun multithread() =
        applyOption(SqliteConfigOption.MULTITHREAD)

    override fun serialized() =
        applyOption(SqliteConfigOption.SERIALIZED)

    override fun pagecache(pMem: Buffer?, sz: Int, n: Int) =
        applyOption(SqliteConfigOption.PAGECACHE(pMem?.buffer, sz, n))

    override fun heap(pMem: Buffer?, nBytes: Int, min: Int) =
        applyOption(SqliteConfigOption.HEAP(pMem?.buffer, nBytes, min))

    override fun memstatus(enabled: Boolean) =
        applyOption(SqliteConfigOption.MEMSTATUS(if (enabled) 1 else 0))

    override fun lookaside(sz: Int, cnt: Int) =
        applyOption(SqliteConfigOption.LOOKASIDE(sz, cnt))

    override fun log(logger: Logger?) = logger
        ?.let { applyOption(SqliteConfigOption.LOG(it, LoggerCallback)) }
        ?: applyOption(SqliteConfigOption.LOG(null, null))

    override fun uri(enabled: Boolean) =
        applyOption(SqliteConfigOption.URI(if (enabled) 1 else 0))

    override fun coveringIndexScan(enabled: Boolean) =
        applyOption(SqliteConfigOption.COVERING_INDEX_SCAN(if (enabled) 1 else 0))

    override fun sqllog(sqlLogger: SqlLogger?) = sqlLogger
        ?.let { applyOption(SqliteConfigOption.SQLLOG(it, SqlLoggerCallback)) }
        ?: applyOption(SqliteConfigOption.SQLLOG(null, null))

    override fun mmapSize(sz: Long, mx: Long) =
        applyOption(SqliteConfigOption.MMAP_SIZE(sz, mx))

    override fun pCacheHdrsz(): Int =
        usingParam(Int32OutputParam(0)) { applyOption(SqliteConfigOption.PCACHE_HDRSZ(it)) }

    override fun pmasz(szPma: UInt) =
        applyOption(SqliteConfigOption.PMASZ(szPma))

    override fun stmtjrnlSpill(nByte: Int) =
        applyOption(SqliteConfigOption.STMTJRNL_SPILL(nByte))

    override fun smallMalloc(enabled: Boolean) =
        applyOption(SqliteConfigOption.SMALL_MALLOC(if (enabled) 1 else 0))

    override fun sorterrefSize(nByte: Int) =
        applyOption(SqliteConfigOption.SORTERREF_SIZE(nByte))

    override fun memdbMaxsize(maxSize: Long) =
        applyOption(SqliteConfigOption.MEMDB_MAXSIZE(maxSize))

    override fun rowidInView(enabled: Boolean?): Boolean? =
        usingBooleanParam(enabled) { applyOption(SqliteConfigOption.ROWID_IN_VIEW(it)) }
}