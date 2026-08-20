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

import ksqlite.internal.test.isWasm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests [SQLite]'s [SQLiteStatic] companion, whose APIs do not require SQLite initialization.
 */
class SQLiteStaticTest {

    @Test
    fun compileOptionsWork() = runTestNoInit {
        assertTrue(SQLite.compileOptions.isNotEmpty())
    }

    @Test
    fun keywordsWork() = runTestNoInit {
        assertTrue(SQLite.keywordCount > 0)

        val keyword = SQLite.getKeyword(0)
        assertTrue(SQLite.isKeyword(keyword))

        assertFalse(SQLite.isKeyword("not_a_keyword_hopefully"))

        assertFailsWith<SQLiteException> {
            SQLite.getKeyword(-1)
        }
    }

    @Test
    fun versionInfoWorks() = runTestNoInit {
        assertTrue(SQLite.version.isNotEmpty())
        assertTrue(SQLite.versionNumber > 0)
        assertTrue(SQLite.sourceId.isNotEmpty())
        assertTrue(SQLite.multipleCiphersVersion.isNotEmpty())
    }

    @Test
    fun isThreadSafeWorks() = runTestNoInit {
        if (!isWasm) {
            assertTrue(SQLite.isThreadSafe)
        }
    }

    @Test
    fun isCompleteSqlStatementWorks() = runTestNoInit {
        assertFalse(SQLite.isCompleteSqlStatement("CREATE TABLE test"))
        assertTrue(SQLite.isCompleteSqlStatement("CREATE TABLE test1; CREATE TABLE test2;"))
    }

    @Test
    fun logWorks() = runTestNoInit {
        // No observable effect without a configured logger, just ensure it doesn't throw.
        SQLite.log(1, "test message")
    }

    @Test
    fun matchGlobWorks() = runTestNoInit {
        assertTrue(SQLite.matchGlob("*.txt", "notes.txt"))
        assertFalse(SQLite.matchGlob("*.txt", "notes.md"))
    }

    @Test
    fun matchLikeWorks() = runTestNoInit {
        assertTrue(SQLite.matchLike("Hello%", "Hello World", 0.toChar()))
        assertFalse(SQLite.matchLike("Goodbye%", "Hello World", 0.toChar()))
    }

    @Test
    fun caseIndependentComparatorWorks() = runTestNoInit {
        assertEquals(0, SQLite.caseIndependentComparator.compare("Hello", "hello"))
        assertTrue(SQLite.caseIndependentComparator.compare("a", "b") < 0)
    }

    @Test
    fun createCaseIndependentComparatorWorks() = runTestNoInit {
        val comparator = SQLite.createCaseIndependentComparator(3)
        assertEquals(0, comparator.compare("Hello", "helWorld"))
    }
}
