package ksqlite.capi

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Tests related to what have been compiled.
 */
class CompilationTest {

    @Test
    fun versionIsCorrect() = runSqliteTest {
        val version = sqlite3_libversion()
        assertEquals("3.53.2", version)

        val versionNumber = sqlite3_libversion_number()
        assertEquals(3053002, versionNumber)

        val sourceId = sqlite3_sourceid()

        assertEquals(
            "2026-06-03 19:12:13 d6e03d8c777cfa2d35e3b60d8ec3e0187f3e9f99d8e2ee9cac695fd6fcdf1a24",
            sourceId
        )
    }

    @Test
    fun compiledWithExpectedOptions() = runSqliteTest {
        val threadSafe = sqlite3_threadsafe()
        assertEquals(1, threadSafe)

        val options = sqliteCompileOptions()

        assertContains(options, "DQS=0")
        assertContains(options, "ENABLE_COLUMN_METADATA")
        assertContains(options, "ENABLE_FTS5")
        assertContains(options, "ENABLE_MATH_FUNCTIONS")
        assertContains(options, "ENABLE_MEMORY_MANAGEMENT")
        assertContains(options, "ENABLE_NORMALIZE")
        assertContains(options, "ENABLE_OFFSET_SQL_FUNC")
        assertContains(options, "ENABLE_PERCENTILE")
        assertContains(options, "ENABLE_PREUPDATE_HOOK")
        assertContains(options, "ENABLE_RTREE")
        assertContains(options, "ENABLE_SNAPSHOT")
        assertContains(options, "ENABLE_SQLLOG")
        assertContains(options, "ENABLE_UNKNOWN_SQL_FUNCTION")
        assertContains(options, "MAX_EXPR_DEPTH=0")
        assertContains(options, "OMIT_AUTOINIT")
        assertContains(options, "OMIT_DEPRECATED")
        assertContains(options, "OMIT_LOAD_EXTENSION")
        assertContains(options, "OMIT_UTF16")
        assertContains(options, "OMIT_SHARED_CACHE")
        assertContains(options, "STRICT_SUBTYPE")
        assertContains(options, "TEMP_STORE=2")
        assertContains(options, "USE_URI")
    }
}