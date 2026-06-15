package ksqlite.kapi

import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.Sqlite3OpenFlag
import kotlin.test.Test

class KapiTest {

    @Test
    fun api() {
        val sqlite = SQLite(
            options = arrayOf(
                SqliteConfigOption.URI(0),
                SqliteConfigOption.LOOKASIDE(0, 0)
            )
        )

        val connection = sqlite.open(
            fileName = "",
            flags = Sqlite3OpenFlag.READWRITE or Sqlite3OpenFlag.URI
        ) {

        }
    }
}