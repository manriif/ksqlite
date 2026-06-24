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
        assertEquals("3.53.2", sqlite3_libversion())
    }

    @Test
    fun compiledWithExpectedOptions() = runSqliteTest {
        val options = sqliteCompileOptions()

        assertContains(options, "ENABLE_FTS5")
        assertContains(options, "ENABLE_COLUMN_METADATA")
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
        assertContains(options, "OMIT_AUTOINIT")
        assertContains(options, "OMIT_DEPRECATED")
        assertContains(options, "OMIT_LOAD_EXTENSION")
        assertContains(options, "OMIT_UTF16")
        assertContains(options, "OMIT_SHARED_CACHE")
        assertContains(options, "TEMP_STORE=2")
        assertContains(options, "USE_URI")
    }
}