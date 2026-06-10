package ksqlite.kapi

import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3OpenFlag
import kotlin.test.Test

class KapiTest {

    @Test
    fun api() {
        val sqlite = SQLite(
            options = arrayOf(
                Sqlite3ConfigOption.URI(0),
                Sqlite3ConfigOption.LOOKASIDE(0, 0)
            )
        )

        val connection = sqlite.open(
            fileName = "",
            flag = Sqlite3OpenFlag.READWRITE or Sqlite3OpenFlag.URI
        ) {

        }
    }
}