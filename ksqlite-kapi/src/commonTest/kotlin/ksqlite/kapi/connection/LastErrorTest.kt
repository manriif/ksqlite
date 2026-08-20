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

/**
 * Tests [LastError].
 */
class LastErrorTest {

    @Test
    fun errorWorks() = runSqliteConnectionTest { _, connection ->
        connection.setExtendedResultCodesEnabled(true)

        assertFailsWith<SQLiteException> {
            connection.execute("""CREATE table fail;""")
        }

        val expectedErrorMessage = """near ";": syntax error"""

        assertEquals(expectedErrorMessage, connection.lastError.message)
        assertEquals(ERROR, connection.lastError.code)
        assertEquals(ERROR, connection.lastError.extendedCode)
        assertEquals(17, connection.lastError.offset)
        assertEquals(0, connection.lastError.systemError)
    }

    @Test
    fun updateWorks() = runSqliteConnectionTest { _, connection ->
        val testMessage = "test error message"
        connection.lastError.update(NOTFOUND, testMessage)

        assertEquals(testMessage, connection.lastError.message)
        assertEquals(NOTFOUND, connection.lastError.code)
    }

    @Test
    fun operationsFailOnceConnectionClosed() = runSqliteConnectionTest { _, connection ->
        val lastError = connection.lastError
        connection.close()

        assertFailsWith<IllegalStateException> { lastError.message }
        assertFailsWith<IllegalStateException> { lastError.code }
        assertFailsWith<IllegalStateException> { lastError.extendedCode }
        assertFailsWith<IllegalStateException> { lastError.offset }
        assertFailsWith<IllegalStateException> { lastError.systemError }
        assertFailsWith<IllegalStateException> { lastError.update(ERROR) }
    }
}
