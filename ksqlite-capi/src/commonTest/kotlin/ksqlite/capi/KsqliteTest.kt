package ksqlite.capi

import kotlin.test.Test
import kotlin.test.assertTrue

class KsqliteTest {

    @Test
    fun version_is_returned() = sqliteTest {
        println(sqlite3_libversion())
        assertTrue { sqlite3_libversion().isNotBlank() }
    }
}