package ksqlite.types.internal

import ksqlite.types.vfs.SqliteIoMethodsVersion
import ksqlite.types.vfs.SqliteVfsVersion

/**
 * [SqliteVfsVersion]s associated by their iVersion.
 */
private val SqliteVfsVersionMap = SqliteVfsVersion.entries.associateBy(SqliteVfsVersion::iVersion)

/**
 * Converts [version] to [SqliteVfsVersion].
 */
public fun convertVfsVersion(version: Int): SqliteVfsVersion =
    checkNotNull(SqliteVfsVersionMap[version]) { "Unknown SQLite VFS version: $version" }

/**
 * [SqliteIoMethodsVersion]s associated by their iVersion.
 */
private val SqliteIoMethodsVersionMap =
    SqliteIoMethodsVersion.entries.associateBy(SqliteIoMethodsVersion::iVersion)

/**
 * Converts [version] to [SqliteIoMethodsVersion].
 */
public fun convertIoMethodsVersion(version: Int): SqliteIoMethodsVersion =
    checkNotNull(SqliteIoMethodsVersionMap[version]) {
        "Unknown SQLite IO methods version $version"
    }