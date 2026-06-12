package ksqlite.kapi.impl

import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.SQLite
import ksqlite.kapi.Connection

internal class SQLiteImpl(
    private val options: List<Sqlite3ConfigOption>,
    private val onClose: () -> Unit
): SQLite {

    fun requireConnection(db: sqlite3): Connection {
        TODO()
    }


}