package ksqlite.kapi.impl

import ksqlite.capi.types.sqlite3
import ksqlite.kapi.SQLiteConnection

internal class SQLiteConnectionImpl(private val db: sqlite3): SQLiteConnection {
}