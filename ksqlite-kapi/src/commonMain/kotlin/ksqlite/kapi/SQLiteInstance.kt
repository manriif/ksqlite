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
package ksqlite.kapi

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_config
import ksqlite.capi.sqlite3_initialize
import ksqlite.capi.sqlite3_shutdown
import ksqlite.capi.sqlite3_stmt
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.config.ConfigurationScopeImpl
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.internal.runtime.closeable.UnsafeCloseableScope
import ksqlite.internal.runtime.concurrency.SafeLock
import ksqlite.internal.runtime.concurrency.withLock
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.statement.PreparedStatement

private var SQLiteInstance: SQLiteImpl? = null
private var SQLiteInstanceLock = SafeLock()

private val sqlite: SQLiteImpl
    get() = checkNotNull(SQLiteInstance) { "No SQLite instance exists or it was closed" }

/**
 * Shutdowns SQLite and clears [SQLiteInstance].
 */
private fun sqliteShutdown() = SQLiteInstanceLock.withLock {
    check(SQLiteInstance != null)
    sqliteResultCheck(sqlite3_shutdown())

    listOf(
        SqliteConfigOption.LOG(null, null),
        SqliteConfigOption.SQLLOG(null, null)
    ).forEach { option ->
        sqliteResultCheck(sqlite3_config(option))
    }

    SQLiteInstance = null
}

/**
 * Initializes SQLite, sets and returns [SQLiteInstance].
 */
internal fun sqliteInitialize(configure: (ConfigurationScope.() -> Unit)? = null): SQLite {
    return SQLiteInstanceLock.withLock {
        check(SQLiteInstance == null) {
            "Only a single instance of SQLite is allowed simultaneously, previous instance must " +
                    "be shutdown first"
        }

        if (configure != null) {
            UnsafeCloseableScope().use { scope ->
                configure(ConfigurationScopeImpl(scope))
            }
        }

        sqliteResultCheck(sqlite3_initialize())

        SQLiteImpl(::sqliteShutdown).also { instance ->
            SQLiteInstance = instance
        }
    }
}

/**
 * Retrieves the [DatabaseConnection] associated with [db].
 */
internal fun sqliteRequireConnection(
    db: sqlite3,
    create: Boolean = false
): DatabaseConnection = SQLiteInstanceLock.withLock { sqlite.requireConnection(db, create) }

/**
 * Retrieves the [PreparedStatement] associated with [stmt].
 */
internal fun sqliteRequireStatement(stmt: sqlite3_stmt): PreparedStatement =
    SQLiteInstanceLock.withLock { sqlite.requireStatement(stmt) }