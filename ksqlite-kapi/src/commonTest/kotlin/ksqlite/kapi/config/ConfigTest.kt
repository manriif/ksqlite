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

import ksqlite.internal.test.isWasm
import ksqlite.kapi.SQLite
import ksqlite.kapi.runSqliteTest
import ksqlite.types.SqliteSqlLogEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests [AnyTimeConfiguration] and [ConfigurationScope].
 */
class ConfigTest {

    @Test
    fun anyTimeConfigurationIsAccessibleAfterInitialization() = runSqliteTest { sqlite ->
        assertTrue(sqlite.config.pageCacheHeaderSize >= 0)
    }

    @Test
    fun loggerWorksAfterInitialization() = runSqliteTest { sqlite ->
        var loggedCode: Int? = null
        var loggedMessage: String? = null

        sqlite.config.setLogger { errorCode, message ->
            loggedCode = errorCode
            loggedMessage = message
        }

        SQLite.log(1234, "test message")
        assertEquals(1234, loggedCode)
        assertEquals("test message", loggedMessage)

        sqlite.config.setLogger(null)
    }

    @Test
    fun loggerCanBeConfiguredBeforeInitialization() {
        var logged = false

        runSqliteTest(configure = {
            setLogger { _, _ -> logged = true }
        }) { sqlite ->
            SQLite.log(1, "message")
            assertTrue(logged)
            sqlite.config.setLogger(null)
        }
    }

    @Test
    fun threadingModeOptionsWork() {
        if (!isWasm) {
            runSqliteTest(configure = { setSingleThread() }) { }
            runSqliteTest(configure = { setMultiThread() }) { }
            runSqliteTest(configure = { setSerialized() }) { }
        }
    }

    @Test
    fun rowidInViewOptionWorks() = runSqliteTest(
        configure = { isRowidInViewActivated = true }
    ) { sqlite ->
        assertTrue(sqlite.config.pageCacheHeaderSize >= 0)
    }

    @Test
    fun miscOptionsDoNotThrow() = runSqliteTest(
        configure = {
            setMemStatusEnabled(true)
            setLookasideConfig(0, 0)
            setUriEnabled(true)
            setCoveringIndexScanEnabled(true)
            setMmapSize(0, 0)
            setPackedMemoryArraySize(1u)
            setStatementJournalSpillThreshold(-1)
            setSmallMallocEnabled(false)
            setInMemoryDatabaseMaxSize(1_000_000)
        }
    ) { }

    @Test
    fun sqlLoggerReceivesLifecycleEvents() {
        val events = mutableListOf<SqliteSqlLogEvent>()

        runSqliteTest(configure = {
            setSqlLogger { _, event -> events.add(event) }
        }) { sqlite ->
            val connection = sqlite.open(":memory:")
            connection.execute("CREATE TABLE fruits(id INTEGER);")
            connection.close()
        }

        assertTrue(events.any { it is SqliteSqlLogEvent.DatabaseOpened })
        assertTrue(events.any { it is SqliteSqlLogEvent.StatementExecuted })
        assertTrue(events.any { it is SqliteSqlLogEvent.DatabaseClosed })
    }
}
