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

import ksqlite.internal.test.concurrent.runConcurrently
import ksqlite.kapi.connection.AutoExtension
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

private const val ThreadCount = 8
private const val ConnectionsPerThread = 20
private const val StatementsPerThread = 20
private const val RegistrationsPerThread = 50
private val Timeout = 30.seconds

/**
 * Concurrency stress tests for the bookkeeping [SQLiteImpl] shares across every connection and
 * statement: `connections`, `statements` and `autoExtensions`.
 */
class SQLiteConcurrencyTest {

    @Test
    fun concurrentConnectionOpenAndCloseDoesNotCorruptBookkeeping() = runSqliteTest { sqlite ->
        // SQLiteImpl.requireConnection() and its close listener both check their bookkeeping and
        // throw if it is inconsistent. A clean run here already proves the connections map stays
        // correct under contention.
        runConcurrently(threadCount = ThreadCount, timeout = Timeout) {
            repeat(ConnectionsPerThread) {
                val connection = sqlite.open(":memory:")
                connection.execute("CREATE TABLE t(v INTEGER);")
                connection.execute("INSERT INTO t(v) VALUES (1);")
                connection.close()
            }
        }
    }

    @Test
    fun concurrentPreparedStatementLifecycleDoesNotCorruptBookkeeping() = runSqliteTest { sqlite ->
        // Every thread owns one connection for its whole run, but SQLiteImpl.statements is a
        // single map shared by every connection.
        runConcurrently(threadCount = ThreadCount, timeout = Timeout) {
            val connection = sqlite.open(":memory:")
            connection.execute("CREATE TABLE t(v INTEGER);")

            try {
                repeat(StatementsPerThread) {
                    connection.prepare("INSERT INTO t(v) VALUES (?);").close()
                }
            } finally {
                connection.close()
            }
        }
    }

    @Test
    fun concurrentAutoExtensionRegistrationDoesNotCorruptBookkeeping() = runSqliteTest { sqlite ->
        val extension = AutoExtension { }

        // Half the threads churn SQLiteImpl.autoExtensions. The other half call open(), which
        // reads a snapshot of it for every new connection. This races that read against the churn.
        runConcurrently(threadCount = ThreadCount, timeout = Timeout) { threadIndex ->
            if (threadIndex % 2 == 0) {
                repeat(RegistrationsPerThread) {
                    sqlite.addAutoExtension(extension)
                    sqlite.removeAutoExtension(extension)
                }
            } else {
                repeat(ConnectionsPerThread) {
                    sqlite.open(":memory:").close()
                }
            }
        }
    }
}