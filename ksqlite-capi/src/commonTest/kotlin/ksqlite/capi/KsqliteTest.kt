package ksqlite.capi

import kotlin.test.Test
import kotlin.test.assertTrue

class KsqliteTest {

    @Test
    fun `version is returned`() = sqliteTest {
        println(sqlite3_libversion())
        assertTrue { sqlite3_libversion().isNotBlank() }
    }
}