package ksqlite

import kotlin.test.Test
import kotlin.test.assertTrue

class KsqliteTest {

    @Test
    fun `version is returned`() = sqliteTest {
        println(sqliteLibVersion)
        assertTrue { sqliteLibVersion.isNotBlank() }
    }
}