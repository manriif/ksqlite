package ksqlite.kapi.config

import ksqlite.capi.sqlite3_config
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.kapi.helpers.UnsafeClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam

internal open class AnyTimeConfigurationImpl :
    AnyTimeConfiguration,
    UnsafeClosableScope() {

    override val pageCacheHeaderSize: Int
        get() = usingParam(Int32OutputParam(0)) { applyOption(SqliteConfigOption.PCACHE_HDRSZ(it)) }

    /**
     * Applies the given configuration [option].
     */
    protected fun applyOption(option: SqliteConfigOption) =
        notClosed { sqliteResultCheck(sqlite3_config(option)) }

    override fun setLogger(logger: Logger?) = logger
        ?.let { applyOption(SqliteConfigOption.LOG(it, LoggerCallback)) }
        ?: applyOption(SqliteConfigOption.LOG(null, null))
}