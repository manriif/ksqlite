package ksqlite.capi.types

import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback

/**
 * Data holder to use with [ksqlite.types.SqliteConfigOption.LOG].
 */
public class SqliteLogger<AppData>(
    internal val appData: AppData,
    internal val callback: SqliteConfigLogCallback<AppData>
)

/**
 * Data holder to use with [ksqlite.types.SqliteConfigOption.SQLLOG].
 */
public class SqliteSqlLogger<AppData>(
    internal val appData: AppData,
    internal val callback: SqliteConfigSqlLogCallback<AppData>
)