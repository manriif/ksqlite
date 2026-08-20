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
package ksqlite.kapi.connection

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteConnectionTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests [DatabaseConnectionConfiguration].
 */
class DatabaseConnectionConfigurationTest {

    @Test
    fun booleanOptionsWork() = runSqliteConnectionTest { _, connection ->
        val config = connection.config

        config.isForeignKeyEnabled = true
        assertTrue(config.isForeignKeyEnabled)
        config.isForeignKeyEnabled = false
        assertTrue(!config.isForeignKeyEnabled)

        config.areTriggersEnabled = false
        assertTrue(!config.areTriggersEnabled)
        config.areTriggersEnabled = true
        assertTrue(config.areTriggersEnabled)

        config.isFts3tokenizerEnabled = true
        assertTrue(config.isFts3tokenizerEnabled)

        config.isLoadExtensionEnabled = false
        assertTrue(!config.isLoadExtensionEnabled)

        config.isCheckpointOnCloseDisabled = true
        assertTrue(config.isCheckpointOnCloseDisabled)
        config.isCheckpointOnCloseDisabled = false

        config.isQueryPlannerStabilityGuaranteeEnabled = false
        assertTrue(!config.isQueryPlannerStabilityGuaranteeEnabled)
        config.isQueryPlannerStabilityGuaranteeEnabled = true

        config.isTriggerExplainQueryPlanEnabled = true
        assertTrue(config.isTriggerExplainQueryPlanEnabled)

        config.isDefensive = true
        assertTrue(config.isDefensive)
        config.isDefensive = false

        config.isWritableSchema = true
        assertTrue(config.isWritableSchema)
        config.isWritableSchema = false

        config.isLegacyAlterTableBehaviorEnabled = true
        assertTrue(config.isLegacyAlterTableBehaviorEnabled)

        config.isDoubleQuotedStringDmlEnabled = false
        assertTrue(!config.isDoubleQuotedStringDmlEnabled)

        config.isDoubleQuotedStringDdlEnabled = true
        assertTrue(config.isDoubleQuotedStringDdlEnabled)

        config.areViewsEnabled = false
        assertTrue(!config.areViewsEnabled)
        config.areViewsEnabled = true

        config.isTrustedSchema = false
        assertTrue(!config.isTrustedSchema)
        config.isTrustedSchema = true

        config.isStatementScanStatusEnabled = true
        assertTrue(config.isStatementScanStatusEnabled)

        config.isReverseScanOrderEnabled = true
        assertTrue(config.isReverseScanOrderEnabled)
        config.isReverseScanOrderEnabled = false

        config.isAttachCreateEnabled = false
        assertTrue(!config.isAttachCreateEnabled)
        config.isAttachCreateEnabled = true

        config.isAttachWriteEnabled = false
        assertTrue(!config.isAttachWriteEnabled)
        config.isAttachWriteEnabled = true

        config.areCommentsEnabled = true
        assertTrue(config.areCommentsEnabled)
    }

    @Test
    fun floatingPointDigitsWorks() = runSqliteConnectionTest { _, connection ->
        connection.config.floatingPointDigits = 10
        assertEquals(10, connection.config.floatingPointDigits)
    }

    @Test
    fun setMainDatabaseNameWorks() = runSqliteConnectionTest { _, connection ->
        connection.config.setMainDatabaseName("theMainDatabase")
        assertEquals("theMainDatabase", connection.getName(0))
    }

    @Test
    fun setResetDatabaseEnabledWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")

        // Resetting the database is only performed on the next VACUUM
        connection.config.setResetDatabaseEnabled(true)
        connection.execute("VACUUM;")
        connection.config.setResetDatabaseEnabled(false)

        assertFailsWith<SQLiteException> {
            connection.tableColumnMetadata("fruits", "id")
        }
    }

    @Test
    fun operationsFailOnceConnectionClosed() = runSqliteConnectionTest { _, connection ->
        val config = connection.config
        connection.close()

        assertFailsWith<IllegalStateException> { config.isForeignKeyEnabled }
        assertFailsWith<IllegalStateException> { config.isForeignKeyEnabled = true }
        assertFailsWith<IllegalStateException> { config.floatingPointDigits }
        assertFailsWith<IllegalStateException> { config.setMainDatabaseName("x") }
        assertFailsWith<IllegalStateException> { config.setResetDatabaseEnabled(true) }
    }
}
