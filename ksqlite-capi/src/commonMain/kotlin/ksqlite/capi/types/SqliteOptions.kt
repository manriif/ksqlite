package ksqlite.capi.types

import ksqlite.capi.memory.Buffer

/**
 * Config option type with capi type parameters.
 */
public typealias CapiSqliteConfigOption =
        ksqlite.types.SqliteConfigOption<Buffer, Int32OutputParam, SqliteLogger<*>, SqliteSqlLogger<*>>

/**
 * Database config option type with capi type parameters.
 */
public typealias CapiSqliteDbConfigOption =
        ksqlite.types.SqliteDbConfigOption<Buffer, Int32OutputParam>