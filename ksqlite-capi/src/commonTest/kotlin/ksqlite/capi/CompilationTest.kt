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
        assertEquals("3.53.3", version)

        val versionNumber = sqlite3_libversion_number()
        assertEquals(3053003, versionNumber)

        val sourceId = sqlite3_sourceid()

        assertEquals(
            "2026-06-26 20:14:12 d4c0e51e4aeb96955b99185ab9cde75c339e2c29c3f3f12428d364a10d782c62",
            sourceId
        )
    }

    @Test
    fun compiledWithExpectedOptions() = runSqliteTest {
        val options = buildList {
            var index = 0
            var option = sqlite3_compileoption_get(index++)

            while (option != null) {
                add(option)
                option = sqlite3_compileoption_get(index++)
            }
        }

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

        val omitAutoInitUsed = sqlite3_compileoption_used("OMIT_AUTOINIT")
        assertEquals(1, omitAutoInitUsed)
    }
}