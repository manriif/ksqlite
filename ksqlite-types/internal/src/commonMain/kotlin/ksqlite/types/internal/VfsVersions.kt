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